package com.skytracker.service;

import com.skytracker.common.exception.AmadeusTokenIssueException;
import com.skytracker.common.exception.DistributedLockTimeoutException;
import com.skytracker.core.constants.RedisKeys;
import com.skytracker.core.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmadeusTokenManger {

    @Value("${spring.amadeus.api.client-id}")
    private String clientId;

    @Value("${spring.amadeus.api.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate;
    private final RedissonClient redisson;
    private final RedisService redisService;

    private static final String ACCESS_URL = "https://test.api.amadeus.com/v1/security/oauth2/token";

    /**
     *  Amadeus API 를 RestTemplate 로 요청, redis 에 TTL 30분으로 저장.
     */
    public String requestNewAccessToken() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(ACCESS_URL, request, Map.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new AmadeusTokenIssueException("HTTP " + response.getStatusCode());
            }

            String token = (String) response.getBody().get("access_token");
            log.info("accessToken = {}", token);
            redisService.setValueWithTTL(RedisKeys.AMADEUS_TOKEN, token, Duration.ofMinutes(30));
            return token;

        } catch (Exception e) {
            log.error("Amadeus Access Token 발급 실패", e);
            throw new AmadeusTokenIssueException("Amadeus Access Token 발급 실패",e);
        }
    }

    /**
     *  redisson Lock 으로 여러 서버에서 요청 하더라도 하나의 요청만 수행할 수 있도록 서비스 로직 구현
     */
    public String getAmadeusAccessToken() {
        String token = redisService.getValue(RedisKeys.AMADEUS_TOKEN);
        if (token != null) return token;

        RLock lock = redisson.getLock(RedisKeys.AMADEUS_TOKEN_LOCK);
        try {
            // 락 획득 성공
            if (lock.tryLock(5, 3, TimeUnit.SECONDS)) {
                try {
                    // 다시 redis 확인 (혹시 다른 스레드가 먼저 처리했을 수 있는 경우)
                    token = redisService.getValue(RedisKeys.AMADEUS_TOKEN);
                    if (token != null) return token;

                    // 진짜 발급
                    return requestNewAccessToken();
                } finally {
                    if (lock.isHeldByCurrentThread()) lock.unlock();
                }
            } else {
                // 락 획득 실패: 누군가 발급 중일 가능성 높음 → soft wait and retry
                log.info("Lock 획득 실패, 다른 서버에서 발급 중일 수 있음. redis polling 시작");

                for (int i = 0; i < 4; i++) { // 최대 4초까지 polling
                    Thread.sleep(1000);
                    token = redisService.getValue(RedisKeys.AMADEUS_TOKEN);
                    if (token != null) return token;
                }

                throw new RuntimeException("다른 서버에서 토큰 발급 지연 중. 나중에 다시 시도해주세요.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DistributedLockTimeoutException("interrupted while waiting for token/lock");
        }
    }

    /**
     *  Scheduled 갱신으로 27분 마다 accessToekn 갱신
     */
    @Scheduled(fixedDelay = 27 * 60 * 1000)
    public void scheduledRefresh() {
        log.info("Amadeus 토큰 스케줄 갱신 시작");
        requestNewAccessToken();
    }


}
