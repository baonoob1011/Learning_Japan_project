package com.example.learningApp.dto.response.friend;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Builder
public class FriendRequestResponse {

    private String requestId;
    private String senderId;
    private String senderName;
    private String senderAvatar;

    private String receiverId;
    private String receiverName;
    private String receiverAvatar;

    private String status;
    private LocalDateTime createdAt;
}