package com.example.learningApp.dto.request.exam.assessment;


import com.example.learningApp.enums.AssessmentType;
import lombok.Data;

@Data
public class UpdateAssessmentItemRequest {

    private String sectionId;
    private String name;
    private String level;
    private Integer questionCount;
    private Float pointPerQuestion;
    private AssessmentType assessmentType;
}