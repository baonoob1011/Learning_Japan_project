package com.example.learningApp.controller.feedback;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.feedback.CreateFeedbackRequest;
import com.example.learningApp.dto.response.feedback.FeedbackResponse;
import com.example.learningApp.service.feeback.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    /**
     * User gửi feedback
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FeedbackResponse>> createFeedback(
            @RequestBody CreateFeedbackRequest request
    ) {

        FeedbackResponse response =
                feedbackService.createFeedback(request);

        return ResponseEntity.ok(
                ApiResponse.success("Send feedback successfully", response)
        );
    }

    /**
     * User xem feedback của mình
     */
    @GetMapping("/my-feedbacks")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getMyFeedbacks() {

        List<FeedbackResponse> responses =
                feedbackService.getMyFeedbacks();

        return ResponseEntity.ok(
                ApiResponse.success("Get my feedbacks successfully", responses)
        );
    }
}