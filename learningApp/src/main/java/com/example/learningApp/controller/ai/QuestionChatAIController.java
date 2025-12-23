package com.example.learningApp.controller.ai;

import com.example.learningApp.dto.ApiResponse;
import com.example.learningApp.dto.request.exam.QuestionChatRequest;
import com.example.learningApp.dto.response.ai.QuestionChatResponse;
import com.example.learningApp.service.ai.QuestionChatAIGeminiService;
import com.example.learningApp.service.ai.QuestionChatAIService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/question-chat")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER_VIP')")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuestionChatAIController {
    QuestionChatAIService chatService;

    @PostMapping("/question")
    public ResponseEntity<ApiResponse<QuestionChatResponse>> chatWithQuestion(
            @RequestBody @Valid QuestionChatRequest request
    ) throws Exception {

        String reply = chatService.chatWithQuestion(
                request.getQuestionId(),
                request.getUserMessage()
        );

        QuestionChatResponse res = QuestionChatResponse.builder()
                .questionId(request.getQuestionId())
                .aiReply(reply)
                .build();

        return ResponseEntity.ok(
                ApiResponse.success("AI reply generated", res)
        );
    }
}
