package com.example.learningApp.dto.response.progress;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLearningProgressResponse {

    private String id;
    private String level;

    private Integer totalExamsTaken;
    private Integer totalQuestionsDone;
    private Integer correctQuestions;

    private Float averageScore;
    private LocalDateTime lastExamAt;
}
