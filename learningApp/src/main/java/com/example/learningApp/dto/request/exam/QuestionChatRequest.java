package com.example.learningApp.dto.request.exam;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionChatRequest {
    private String questionId;
    private String userMessage;
}
