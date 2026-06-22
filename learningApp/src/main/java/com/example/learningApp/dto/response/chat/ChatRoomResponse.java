package com.example.learningApp.dto.response.chat;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ChatRoomResponse {

    private String id;
    private String roomType;
    private LocalDateTime createdAt;

    private List<ChatRoomMemberResponse> members;
    private String name;
    private String avatarUrl;
    // display
    private String otherUserName;
    private String otherUserAvatar;

    // last message
    private String lastMessage;
    private LocalDateTime lastMessageTime;

    private Integer unreadCount;
}

