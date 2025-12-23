package com.example.learningApp.dto.response.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionChatResponse {

    private String questionId;
    private String aiReply;
}
