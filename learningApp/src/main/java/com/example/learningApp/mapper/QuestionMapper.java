package com.example.learningApp.mapper;

import com.example.learningApp.dto.request.exam.CreateQuestionRequest;
import com.example.learningApp.dto.request.exam.question.UpdateQuestionRequest;
import com.example.learningApp.dto.response.exam.PassageResponse;
import com.example.learningApp.dto.response.exam.QuestionResponse;
import com.example.learningApp.entity.Passage;
import com.example.learningApp.entity.Question;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.*;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface QuestionMapper {

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /* ================= CREATE ================= */

    @Mapping(target = "options", source = "options")
    Question toQuestion(CreateQuestionRequest createQuestionRequest);

    /* ================= RESPONSE ================= */

    @Mapping(target = "options", source = "options")
    QuestionResponse toQuestionResponse(Question question);

    PassageResponse toPassageResponse(Passage passage);

    /* ================= UPDATE ================= */

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "options", source = "options")
    void updateQuestion(
            @MappingTarget Question entity,
            UpdateQuestionRequest request);

    /* ================= CUSTOM MAPPING ================= */

    // List<String> → JSON String
    default String map(List<String> value) {
        if (value == null)
            return null;
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting List to JSON", e);
        }
    }

    // JSON String → List<String>
    default List<String> map(String value) {
        if (value == null || value.isEmpty())
            return Collections.emptyList();
        try {
            return OBJECT_MAPPER.readValue(
                    value,
                    OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting JSON to List", e);
        }
    }
}