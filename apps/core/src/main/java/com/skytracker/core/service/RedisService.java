package com.skytracker.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public void pushList(String key, String json) {
        redisTemplate.opsForList().rightPush(key, json);
    }

    public void cachingList(String key, String json) {
        redisTemplate.opsForList().rightPush(key, json, Duration.ofMinutes(10));
        redisTemplate.opsForList().trim(key, -100, -1);
    }

    public List<Object> getHashValues(String key) {
        return redisTemplate.opsForHash().values(key);
    }

    public Map<Object, Object> getHash(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public void rename(String fromKey, String toKey) {
        redisTemplate.rename(fromKey, toKey);
    }

    public void hashPut(String key, String field, String value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    public void setValueWithTTL(String key, String value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    public String getValue(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }
}
