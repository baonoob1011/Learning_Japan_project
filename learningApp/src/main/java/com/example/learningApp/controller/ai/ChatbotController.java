package com.example.learningApp.controller.ai;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.ai.ChatRequest;
import com.example.learningApp.dto.response.ai.ChatResponse;
import com.example.learningApp.service.ai.ChatbotService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(" ")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatbotController {

    ChatbotService chatbotService;

    @PostMapping
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @RequestBody ChatRequest request
    ) {
        String reply = chatbotService.chat(request.getMessage());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Chat with AI successfully",
                        new ChatResponse(reply)
                )
        );
    }
}
