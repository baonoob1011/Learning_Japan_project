package com.example.learningApp.mapper;

import com.example.learningApp.dto.request.exam.CreateQuestionRequest;
import com.example.learningApp.dto.request.exam.question.UpdateQuestionRequest;
import com.example.learningApp.dto.response.exam.PassageResponse;
import com.example.learningApp.dto.response.exam.QuestionResponse;
import com.example.learningApp.entity.Passage;
import com.example.learningApp.entity.Question;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface QuestionMapper {

    /* ================= CREATE ================= */

    Question toQuestion(CreateQuestionRequest createQuestionRequest);

    /* ================= RESPONSE ================= */

    @Mapping(target = "sectionOrder", source = "section.sectionOrder")
    QuestionResponse toQuestionResponse(Question question);

    PassageResponse toPassageResponse(Passage passage);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateQuestion(
            @MappingTarget Question entity,
            UpdateQuestionRequest request);
}