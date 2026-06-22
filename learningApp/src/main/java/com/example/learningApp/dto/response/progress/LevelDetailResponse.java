package com.example.learningApp.dto.response.progress;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LevelDetailResponse {
    private String level;
    private Integer totalExamsTaken;
    private Integer totalQuestionsDone;
    private Integer correctQuestions;
    private Double accuracy;
    private LocalDateTime lastExamAt;
}

