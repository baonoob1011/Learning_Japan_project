package com.example.learningApp.dto.response.review;

import com.example.learningApp.enums.ReviewSessionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ReviewSessionResponse {
    private String id;
    private LocalDate date;
    private ReviewSessionStatus status;
    private int dueCount;
    private int overdueInjectedCount;
    private int newCount;
    private int totalCount;
}

