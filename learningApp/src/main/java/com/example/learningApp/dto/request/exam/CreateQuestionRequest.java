package com.example.learningApp.dto.request.exam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CreateQuestionRequest {
    @NotNull
    private String sectionId;
    @NotBlank
    private String type;
    @NotBlank
    private String questionText;
    private String options;      // JSON string
    private String answer;       // JSON string
    private String imageUrl;
    private String audioUrl;
    @NotNull
    private Integer sectionOrder;
}
