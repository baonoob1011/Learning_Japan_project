package com.example.learningApp.mapper;


import com.example.learningApp.dto.request.chat.CreateChatMessageRequest;
import com.example.learningApp.dto.response.chat.ChatMessageResponse;
import com.example.learningApp.entity.ChatMessage;
import com.example.learningApp.entity.ChatRoom;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface ChatMessageMapper {

    @Mapping(target = "roomId", source = "room.id")
    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "senderName", source = "sender.fullName") // ✅ đúng field
    ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage);

    ChatMessage toChatMessage(CreateChatMessageRequest request);
}
