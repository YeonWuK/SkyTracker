package com.skytracker.core.service;

import com.skytracker.common.exception.integrations.RouteAggregationException;
import com.skytracker.common.exception.integrations.RouteKeyNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
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
        redisTemplate.opsForList().trim(key, -10, -1);
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
        redisTemplate.opsForList().trim(key, -20, -1);
        redisTemplate.expire(key, Duration.ofMinutes(13));
    }

    /**
     * Redis에 저장된 최저가 문자열을 long으로 변환해 반환한다.
     */
    public long getMinPrice(String key) {
        String value = (String) redisTemplate.opsForValue().get(key);
        if (value == null) {
            throw new RouteKeyNotFoundException("최저가 Redis key를 찾을 수 없습니다. key=" + key);
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new RouteAggregationException("유효하지 않은 최저가 값입니다. key=" + key + ", value=" + value);
        }
    }

    /**
     * 기존 호출부 호환용 메서드다. 신규 코드는 getMinPrice를 사용한다.
     */
    @Deprecated
    public long getminPrice(String key) {
        return getMinPrice(key);
    }
}
