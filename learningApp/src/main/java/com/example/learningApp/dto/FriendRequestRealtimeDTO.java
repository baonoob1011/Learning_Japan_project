package com.example.learningApp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FriendRequestRealtimeDTO {

    private String requestId;
    private String senderId;
    private String senderName;
    private String senderAvatar;
    private String status;
}
