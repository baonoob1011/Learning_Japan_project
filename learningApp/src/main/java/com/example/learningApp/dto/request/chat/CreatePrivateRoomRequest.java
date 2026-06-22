package com.example.learningApp.dto.request.chat;

import lombok.Data;

@Data
public class CreatePrivateRoomRequest {
    private String targetUserId; // người mình muốn chat
}

