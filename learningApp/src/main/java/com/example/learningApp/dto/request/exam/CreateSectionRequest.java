package com.example.learningApp.dto.request.exam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CreateSectionRequest {
    @NotNull
    private String examId;
    @NotBlank
    private String title;
    @NotNull
    private Integer orderNum;
}

