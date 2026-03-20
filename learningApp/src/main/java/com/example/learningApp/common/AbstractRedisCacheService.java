package com.example.learningApp.common;


import com.example.learningApp.common.cache.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

@RequiredArgsConstructor
public abstract class AbstractRedisCacheService<T>
        implements CacheService<T> {

    protected final RedisTemplate<String, Object> redisTemplate;

    protected abstract String prefix();
    protected abstract Duration ttl();
    protected abstract Class<T> type();

    protected String buildKey(String key) {
        return prefix() + key;
    }

    @Override
    public void save(String key, T value) {
        redisTemplate.opsForValue().set(
                buildKey(key),
                value,
                ttl()
        );
    }

    public T get(String key) {
        Object value = redisTemplate.opsForValue().get(buildKey(key));
        if (value == null) {
            return null;
        }
        return type().cast(value);
    }
}

