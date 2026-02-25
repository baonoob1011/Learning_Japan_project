package com.example.learningApp.dto.response.friend;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendRequestDTO {
    private String requestId;
    private String senderId;
    private String senderName;
    private String senderAvatar;
}
