package com.example.learningApp.mapper;

import com.example.learningApp.dto.cache.VocabCache;
import com.example.learningApp.entity.Vocab;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VocabCacheMapper {
    VocabCache toCache(Vocab vocab);
}

