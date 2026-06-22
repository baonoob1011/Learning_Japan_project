package com.example.learningApp.controller.review;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.common.PageResponse;
import com.example.learningApp.dto.request.review.GradeReviewRequest;
import com.example.learningApp.dto.response.review.GradeReviewResponse;
import com.example.learningApp.dto.response.review.TodayReviewQueueResponse;
import com.example.learningApp.dto.response.vocab.UserVocabProgressResponse;
import com.example.learningApp.service.review.ReviewService;
import com.example.learningApp.service.review.ReviewSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewSessionService reviewSessionService;
    private final ReviewService reviewService;

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<TodayReviewQueueResponse>> getTodayReviews() {
        return ResponseEntity.ok(ApiResponse.success("Fetched today's review queue", reviewSessionService.getTodayQueueForCurrentUser()));
    }

    @PostMapping("/{wordProgressId}/grade")
    public ResponseEntity<ApiResponse<GradeReviewResponse>> grade(
            @PathVariable String wordProgressId,
            @Valid @RequestBody GradeReviewRequest request
    ) {
        GradeReviewResponse response = reviewService.grade(wordProgressId, request.getGrade());
        return ResponseEntity.ok(ApiResponse.success("Reviewed word successfully", response));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PageResponse<UserVocabProgressResponse>>> getReviewHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success("Fetched review history", reviewService.getHistory(page, size)));
    }
}

