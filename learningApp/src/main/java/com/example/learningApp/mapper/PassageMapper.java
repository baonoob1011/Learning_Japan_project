package com.example.learningApp.mapper;

import com.example.learningApp.dto.request.exam.UpdatePassageRequest;
import com.example.learningApp.dto.response.exam.PassageResponse;
import com.example.learningApp.entity.Passage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PassageMapper {

    PassageResponse toPassageResponse(Passage passage);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "section", ignore = true)
    @Mapping(target = "questions", ignore = true)
    Passage toPassage(UpdatePassageRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "section", ignore = true)
    @Mapping(target = "questions", ignore = true)
    void updatePassageFromRequest(UpdatePassageRequest request, @MappingTarget Passage passage);
}

