package com.example.learningApp.dto.response.review;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TodayReviewSummaryResponse {
    private int newCount;
    private int dueCount;
    private int overdueCount;
    private int todayQueueCount;
}

