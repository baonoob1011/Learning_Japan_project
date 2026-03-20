package com.example.learningApp.dto.response.exam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class ExamResponse {
    private String id;
    private String code;
    private long participant;
    private String level;
    private Integer duration;
    private Integer numSections;
    private Integer numQuestions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

