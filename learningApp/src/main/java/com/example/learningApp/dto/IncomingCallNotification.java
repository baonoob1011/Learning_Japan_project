package com.example.learningApp.dto;

import lombok.Data;

@Data
public class IncomingCallNotification {
    private String type = "incoming";
    private String callType = "VIDEO"; // VIDEO or VOICE
    private String roomId;
    private String callerId;
    private String callerName;
    private String callerAvatar;
    private String receiverId;
}
