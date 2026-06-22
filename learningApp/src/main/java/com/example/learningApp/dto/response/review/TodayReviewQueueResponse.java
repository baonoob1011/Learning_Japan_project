package com.example.learningApp.dto.response.review;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TodayReviewQueueResponse {
    private String sessionId;
    private TodayReviewSummaryResponse summary;
    private List<ReviewWordItemResponse> todayQueue;
    private String recoveryMessage;
}

