package com.example.learningApp.dto.response.chat;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageResponse {
    private String roomId;
    private String senderId;   // ✅ thêm dòng này
    private String senderName;
    private String content;
    private LocalDateTime sentAt;
}


