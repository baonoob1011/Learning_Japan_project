package com.example.learningApp.service.translate;


import com.example.learningApp.common.cache.CacheService;
import com.example.learningApp.dto.cache.VocabCache;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis implementation for caching vocabulary data.
 */
@Service
@RequiredArgsConstructor
public class RedisVocabCacheService implements CacheService<VocabCache> {

    private static final String PREFIX = "vocabCache:";
    private static final Duration TTL = Duration.ofHours(1);

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void save(String key, VocabCache value) {
        redisTemplate.opsForValue().set(
                PREFIX + key,
                value,
                TTL
        );
    }
}
