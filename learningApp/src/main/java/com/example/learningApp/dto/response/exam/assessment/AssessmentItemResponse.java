package com.example.learningApp.dto.response.exam.assessment;


import com.example.learningApp.enums.AssessmentType;
import lombok.Data;


import lombok.Builder;

import java.time.LocalDateTime;

@Data
@Builder
public class AssessmentItemResponse {

    private String id;
    private String name;
    private String level;
    private Integer questionCount;
    private Float pointPerQuestion;
    private Float totalPoint;
    private AssessmentType assessmentType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}