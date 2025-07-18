package com.skytracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmadeusTokenManger {

    private final AmadeusService amadeusService;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String TOKEN_KEY = "amadeus:access_token";

    public synchronized String getAccessToken() {
        String token = redisTemplate.opsForValue().get(TOKEN_KEY);
        if (token != null) {
            String accessTokenFromAmadeus = amadeusService.getAccessTokenFromAmadeus();
            return accessTokenFromAmadeus;
        }
        return token;
    }




}
