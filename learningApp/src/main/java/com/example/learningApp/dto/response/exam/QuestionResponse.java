package com.example.learningApp.dto.response.exam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class QuestionResponse {
    private String id;
    private String sectionId;
    private String type;
    private String questionText;
    private String options;
    private String answer;
    private String imageUrl;
    private String audioUrl;
    private Integer orderNum;
}
