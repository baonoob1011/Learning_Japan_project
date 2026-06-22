package com.example.learningApp.dto.request.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateChatMessageRequest {
    @NotBlank(message = "RoomId cannot be blank")
    private String roomId;

    @NotBlank(message = "Message content cannot be blank")
    private String content;
}

