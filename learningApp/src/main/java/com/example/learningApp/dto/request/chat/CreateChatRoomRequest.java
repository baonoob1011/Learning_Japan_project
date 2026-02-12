package com.example.learningApp.dto.request.chat;

import com.example.learningApp.enums.RoomType;
import lombok.Data;

@Data
public class CreateChatRoomRequest {

    private String name;
    private RoomType roomType;
    private String lessonId;
}
