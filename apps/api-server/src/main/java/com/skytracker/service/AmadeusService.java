package com.skytracker.service;

import com.skytracker.dto.FlightSearchRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class AmadeusService {

    @Value("${spring.amadeus.api.client-id}")
    private String clientId;

    @Value("${spring.amadeus.api.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redisson;

    private static final String TOKEN_KEY = "amadeus:access_token";
    private static final String LOCK_KEY = "lock:amadeus:access_token";
    private static final String ACCESS_URL = "https://test.api.amadeus.com/v1/security/oauth2/token";

    public String getAccessTokenFromAmadeus() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(ACCESS_URL, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String token = (String) response.getBody().get("access_token");
                log.info("accessToken = {}", token);
                redisTemplate.opsForValue().set(TOKEN_KEY, token, Duration.ofMinutes(30));
                return token;
            } else {
                throw new RuntimeException("Failed to get access token");
            }
        } catch (Exception e) {
            log.error("Get Amadeus Access Token 갱신 중 예외 실패", e);
            throw new RuntimeException("Amadeus AccessToken 발급 중 예외 발생", e);
        }
    }


    public String searchFlights(String accessToken, FlightSearchRequestDto req) {
        String url = UriComponentsBuilder.fromHttpUrl("https://test.api.amadeus.com/v2/shopping/flight-offers")
                .queryParam("originLocationCode", req.getOriginLocationCode())
                .queryParam("destinationLocationCode", req.getDestinationLocationCode())
                .queryParam("departureDate", req.getDepartureDate())
                .queryParam("adults", req.getAdults())
                .queryParam("nonStop", req.isNonStop())
                .queryParam("travelClass", req.getTravelClass().getValue())
                .queryParam("currencyCode", req.getCurrencyCode())
                .queryParam("max", req.getMax())
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Flight search result = {}", response.getBody());
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to search flights");
        }
    }

    public String getAmadeusAccessToken() {
        String token = redisTemplate.opsForValue().get(TOKEN_KEY);
        if (token != null) return token;

        RLock lock = redisson.getLock(LOCK_KEY);
        try {
            if (!lock.tryLock(5, 3, TimeUnit.SECONDS)) {
                throw new RuntimeException("Failed to acquire lock");
            }
                token = redisTemplate.opsForValue().get(TOKEN_KEY);
                if (token != null) return token;

                return getAccessTokenFromAmadeus();

        } catch (InterruptedException e) {
            throw new RuntimeException("Failed to acquire lock", e);
        }  finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Scheduled(fixedDelay = 27 * 60 * 1000)
    public void scheduledRefresh() {
        log.info("Amadeus 토큰 스케줄 갱신 시작");
        getAccessTokenFromAmadeus();
    }


}
