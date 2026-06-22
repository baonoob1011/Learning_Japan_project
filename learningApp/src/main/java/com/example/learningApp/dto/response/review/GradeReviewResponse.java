package com.example.learningApp.dto.response.review;

import com.example.learningApp.enums.LearningStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GradeReviewResponse {
    private String wordProgressId;
    private LearningStatus status;
    private int intervalDays;
    private double easeFactor;
    private int lapseCount;
    private int successCount;
    private LocalDateTime nextReviewAt;
}

