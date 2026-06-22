package com.example.learningApp.mapper;


import com.example.learningApp.dto.request.exam.assessment.UpdateAssessmentItemRequest;
import com.example.learningApp.dto.response.exam.assessment.AssessmentItemResponse;
import com.example.learningApp.entity.AssessmentItem;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AssessmentItemMapper {

    /* ===================== ENTITY -> RESPONSE ===================== */

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "level", source = "level")
    @Mapping(target = "questionCount", source = "questionCount")
    @Mapping(target = "pointPerQuestion", source = "pointPerQuestion")
    @Mapping(target = "totalPoint", source = "totalPoint")
    @Mapping(target = "assessmentType", source = "assessmentType")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    AssessmentItemResponse toResponse(AssessmentItem entity);


    /* ===================== UPDATE ===================== */
    // Chỉ update field != null
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(
            @MappingTarget AssessmentItem entity,
            UpdateAssessmentItemRequest request
    );
}
