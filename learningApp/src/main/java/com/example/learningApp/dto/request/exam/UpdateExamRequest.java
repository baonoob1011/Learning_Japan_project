package com.example.learningApp.dto.request.exam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateExamRequest {
    private String code; // VD: JLPT-N1 07 2024

    private String level; // N1/N2/N3/N4/N5

    private Integer duration; // phút

    private Integer numSections;

    private Integer numQuestions;
}
