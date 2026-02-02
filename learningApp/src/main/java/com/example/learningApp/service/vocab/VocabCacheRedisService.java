package com.example.learningApp.service.vocab;

import com.example.learningApp.common.AbstractRedisCacheService;
import com.example.learningApp.common.cache.CacheService;
import com.example.learningApp.dto.cache.VocabCache;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class VocabCacheRedisService
        extends AbstractRedisCacheService<VocabCache> {

    public VocabCacheRedisService(RedisTemplate<String, Object> redisTemplate) {
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


