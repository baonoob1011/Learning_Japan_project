package com.example.learningApp.dto.request.feedback;

import com.example.learningApp.enums.FeedbackType;
import lombok.Data;

@Data
public class CreateFeedbackRequest {
    private FeedbackType type;
    private String content;
    private String attachmentUrl;
}
