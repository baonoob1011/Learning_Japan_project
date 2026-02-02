package com.example.learningApp.common.cache;

public interface CacheService<T> {

    void save(String key, T value);
}
