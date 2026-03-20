package com.example.learningApp.controller.chat;

import com.example.learningApp.dto.request.chat.CreateChatMessageRequest;
import com.example.learningApp.service.chat.ChatMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat.send")
    public void send(
            CreateChatMessageRequest request,
            Principal principal
    ) {
        chatMessageService.saveAndSend(request, principal);
    }

}

