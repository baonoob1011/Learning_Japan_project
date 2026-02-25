package com.example.learningApp.dto.response.progress;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLearningDashboardResponse {

    private String userId;

    // ===== Tổng quan =====
    private int totalExamsTaken;
    private int totalQuestionsDone;
    private int correctQuestions;
    private float accuracy; // %

    private String lastLevel;
    private LocalDateTime lastExamAt;

    // ===== Theo level =====
    private List<LevelProgressDto> levels;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LevelProgressDto {
        private String level;
        private int totalExamsTaken;
        private int totalQuestionsDone;
        private int correctQuestions;
        private Float averageScore;
        private float accuracy;
        private LocalDateTime lastExamAt;
    }
}
