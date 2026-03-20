package com.example.learningApp.service.translate;

import com.example.learningApp.common.AbstractRedisCacheService;
import com.example.learningApp.dto.cache.VocabCache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis implementation for caching vocabulary data.
 */
@Service
public class RedisVocabCacheService
        extends AbstractRedisCacheService<VocabCache> {

    public RedisVocabCacheService(RedisTemplate<String, Object> redisTemplate) {
        super(redisTemplate);
    }

    @Override
    protected String prefix() {
        return "vocabCache:";
    }

    @Override
    protected Duration ttl() {
        return Duration.ofHours(1);
    }

    @Override
    protected Class<VocabCache> type() {
        return VocabCache.class;
    }
}

