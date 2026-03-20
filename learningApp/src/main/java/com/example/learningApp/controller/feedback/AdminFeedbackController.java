package com.example.learningApp.controller.feedback;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.feedback.AdminUpdateFeedbackRequest;
import com.example.learningApp.dto.response.feedback.FeedbackResponse;
import com.example.learningApp.enums.FeedbackStatus;
import com.example.learningApp.service.feeback.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/feedbacks")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminFeedbackController {

    private final FeedbackService feedbackService;

    /**
     * Lấy tất cả feedback
     * Có thể filter theo status (optional)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getAllFeedbacks(
            @RequestParam(required = false) FeedbackStatus status
    ) {

        List<FeedbackResponse> responses;

        if (status != null) {
            responses = feedbackService.getFeedbacksByStatus(status);
        } else {
            responses = feedbackService.getAllFeedbacks();
        }

        return ResponseEntity.ok(
                ApiResponse.success("Get feedback list successfully", responses)
        );
    }
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FeedbackResponse>> updateFeedback(
            @PathVariable String id,
            @RequestBody AdminUpdateFeedbackRequest request
    ) {

        FeedbackResponse response =
                feedbackService.updateFeedback(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Update feedback successfully", response)
        );
    }
}
