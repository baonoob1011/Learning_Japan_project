package com.example.learningApp.dto.request.exam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CreateExamRequest {
    @NotBlank(message = "Exam code is required")
    private String code;        // VD: JLPT-N1 07 2024

    @NotBlank(message = "Level is required")
    private String level;       // N1/N2/N3/N4/N5

    @NotNull(message = "Duration is required")
    private Integer duration;   // phút

    @NotNull(message = "Number of sections is required")
    private Integer numSections;

    @NotNull(message = "Number of questions is required")
    private Integer numQuestions;
}

