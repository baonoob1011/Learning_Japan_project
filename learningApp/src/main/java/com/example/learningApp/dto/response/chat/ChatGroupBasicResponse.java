package com.example.learningApp.dto.response.chat;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatGroupBasicResponse {

    private String id;
    private String roomType;
    private LocalDateTime createdAt;

    private String name;
    private String avatarUrl;

    private String lastMessage;
    private LocalDateTime lastMessageTime;

    private int unreadCount;
    private int memberCount;
}
