package com.example.learningApp.dto.response.exam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class SectionResponse {
    private String id;
    private String examId;
    private String title;
    private Integer orderNum;
}

