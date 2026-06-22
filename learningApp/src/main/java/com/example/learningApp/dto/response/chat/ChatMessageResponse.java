package com.example.learningApp.dto.response.chat;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Data
public class ChatMessageResponse {
    private String id;
    private String roomId;
    private String senderId; // ✅ thêm dòng này
    private String receiverId; // ✅ thêm dòng này
    private String senderName;
    private String content;

    private String type;
    private String callType;
    private String callStatus;
    private String callSessionId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime sentAt;
}
