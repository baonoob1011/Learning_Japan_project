package com.example.learningApp.mapper;


import com.example.learningApp.dto.request.chat.CreateChatRoomRequest;
import com.example.learningApp.dto.response.chat.ChatRoomResponse;
import com.example.learningApp.entity.ChatRoom;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface ChatRoomMapper {
    ChatRoom toChatRoom(CreateChatRoomRequest request);

    ChatRoomResponse toChatRoomResponse(ChatRoom room);
}

