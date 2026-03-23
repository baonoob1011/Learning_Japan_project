package com.example.learningApp.mapper;

import com.example.learningApp.dto.cache.VocabCache;
import com.example.learningApp.entity.Vocab;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VocabCacheMapper {
    @org.mapstruct.Mapping(target = "example", source = "explain")
    @org.mapstruct.Mapping(target = "exampleJa", ignore = true)
    @org.mapstruct.Mapping(target = "exampleVi", ignore = true)
    VocabCache toCache(Vocab vocab);
}
