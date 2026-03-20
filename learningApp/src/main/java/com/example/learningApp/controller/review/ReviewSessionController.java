package com.example.learningApp.controller.review;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.common.PageResponse;
import com.example.learningApp.dto.response.review.ReviewSessionResponse;
import com.example.learningApp.dto.response.review.TodayReviewQueueResponse;
import com.example.learningApp.service.review.ReviewSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/review-sessions")
@RequiredArgsConstructor
public class ReviewSessionController {

    private final ReviewSessionService reviewSessionService;

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<TodayReviewQueueResponse>> getTodaySession() {
        return ResponseEntity.ok(ApiResponse.success("Fetched today's review session", reviewSessionService.getTodayQueueForCurrentUser()));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PageResponse<ReviewSessionResponse>>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success("Fetched review session history", reviewSessionService.getMySessionHistory(page, size)));
    }
}

