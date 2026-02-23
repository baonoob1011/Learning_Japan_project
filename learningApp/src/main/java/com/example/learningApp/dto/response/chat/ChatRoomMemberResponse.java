package com.example.learningApp.dto.response.chat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatRoomMemberResponse {

    private String userId;
    private String fullName;
    private String avatarUrl;
}