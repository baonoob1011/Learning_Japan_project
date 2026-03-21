package com.example.learningApp.dto.response.review;

import com.example.learningApp.enums.LearningStatus;
import com.example.learningApp.enums.ReviewQueueType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewWordItemResponse {
    private String wordProgressId;
    private String vocabId;
    private String word;
    private String meaning;
    private ReviewQueueType type;
    private LearningStatus status;
    private int lapseCount;
    private int intervalDays;
    private LocalDateTime nextReviewAt;
    private boolean completed;
}


