package com.example.learningApp.dto.response.exam;

import com.example.learningApp.enums.AssessmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class QuestionResponse {
    private String id;
    private Integer sectionOrder;
    private AssessmentType questionType;
    private String questionText;
    private String options;
    private String answer;
    private String imageUrl;
    private String audioUrl;
    private Integer questionOrder;
}
