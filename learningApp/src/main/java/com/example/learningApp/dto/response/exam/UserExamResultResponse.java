package com.example.learningApp.dto.response.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserExamResultResponse {
    private String userId;
    private String examId;
    private int totalQuestions;
    private int correctQuestions;
    private float score;
    private LocalDateTime submittedAt;
}
