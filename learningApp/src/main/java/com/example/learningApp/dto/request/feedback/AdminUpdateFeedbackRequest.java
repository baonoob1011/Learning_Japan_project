package com.example.learningApp.dto.request.feedback;

import com.example.learningApp.enums.FeedbackStatus;
import lombok.Data;

@Data
public class AdminUpdateFeedbackRequest {

    private FeedbackStatus status;
    private String adminReply;
}
