package com.example.learningApp.dto.response.chat;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PrivateChatPreviewResponse {
    private String roomId;   // 👈 thêm cái này

    private String userId;
    private String fullName;
    private String avatarUrl;

    private String lastMessage;
    private LocalDateTime lastMessageTime;
}