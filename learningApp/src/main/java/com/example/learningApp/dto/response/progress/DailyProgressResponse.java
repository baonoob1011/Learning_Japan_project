package com.example.learningApp.dto.response.progress;


import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyProgressResponse {

    private LocalDate date;

    private int totalExamsTaken;
    private int totalQuestionsDone;
    private int correctQuestions;

    private float accuracy; // %
}
