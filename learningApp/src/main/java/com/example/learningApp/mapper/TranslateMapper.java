package com.example.learningApp.mapper;

import com.example.learningApp.dto.cache.VocabCache;
import com.example.learningApp.dto.response.translate.TranslateResponse;
import com.example.learningApp.entity.Vocab;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TranslateMapper {
    @Mapping(target = "videoId", ignore = true)
    TranslateResponse toTranslateResponse(VocabCache cache, @Context String videoId);

    default TranslateResponse mapWithVideoId(VocabCache cache, String videoId) {
        TranslateResponse res = toTranslateResponse(cache, videoId);
        res.setVideoId(videoId);
        return res;
    }

    @Mapping(target = "videoId", source = "videoId")
    @Mapping(target = "example", source = "explain")
    TranslateResponse toTranslateResponse(Vocab vocab, String videoId);
}
