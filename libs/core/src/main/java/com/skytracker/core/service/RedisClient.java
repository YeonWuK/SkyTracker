package com.skytracker.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisClient {

    private final RedisTemplate<String, Object> redisTemplate;

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public void pushList(String key, String json) {
        redisTemplate.opsForList().rightPush(key, json);
    }

    public List<String> getList(String key) {
        List<Object> values = redisTemplate.opsForList().range(key, 0, -1);
        return values == null ? List.of() :
                values.stream().map(String::valueOf).toList();
    }

    public void rename(String fromKey, String toKey) {
        redisTemplate.rename(fromKey, toKey);
    }

    public void setValueWithTTL(String key, String value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    public String getValue(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }

    public Boolean isBlackListed(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value.toString());
    }

    public void setBlackList(String key, boolean value, long ttl, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, String.valueOf(value), ttl, unit);
    }

    public void kafkaPushList(String key, String json) {
        redisTemplate.opsForList().rightPush(key, json);
        redisTemplate.expire(key, Duration.ofMinutes(10));
    }

    public Integer getminPrice(String key) {
        String value = (String) redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        return Integer.parseInt(value);
    }
}
