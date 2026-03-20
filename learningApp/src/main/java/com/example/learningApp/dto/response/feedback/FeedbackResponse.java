package com.example.learningApp.dto.response.feedback;

import com.example.learningApp.enums.FeedbackStatus;
import com.example.learningApp.enums.FeedbackType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FeedbackResponse {
    private String id;
    private String userId;
    private FeedbackType type;
    private String content;
    private String attachmentUrl;
    private FeedbackStatus status;
    private LocalDateTime createdAt;
    private String adminReply;
    private LocalDateTime resolvedAt;
}
