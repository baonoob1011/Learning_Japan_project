package com.example.learningApp.service.chat;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.request.chat.CreateChatMessageRequest;
import com.example.learningApp.dto.response.chat.ChatMessageResponse;
import com.example.learningApp.entity.ChatMessage;
import com.example.learningApp.entity.ChatRoom;
import com.example.learningApp.entity.User;
import com.example.learningApp.mapper.ChatMessageMapper;
import com.example.learningApp.repository.ChatMessageRepository;
import com.example.learningApp.repository.ChatRoomMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageMapper chatMessageMapper;
    private final ChatMessageRepository messageRepository;
    private final ChatRoomMemberRepository memberRepository; // ✅ THÊM CÁI NÀY
    private final EntityFinder finder;

    @Transactional
    public ChatMessageResponse saveAndSend(
            CreateChatMessageRequest request,
            Principal principal
    ) {

        if (principal == null) {
            throw new RuntimeException("Unauthenticated");
        }

        if (request.getRoomId() == null || request.getRoomId().isBlank()) {
            throw new RuntimeException("RoomId cannot be empty");
        }

        String content = request.getContent() == null
                ? ""
                : request.getContent().trim();

        if (content.isEmpty()) {
            throw new RuntimeException("Message content cannot be empty");
        }

        if (content.length() > 2000) {
            throw new RuntimeException("Message is too long");
        }

        ChatRoom room = finder.chatRoomById(request.getRoomId());

        String userId = principal.getName();
        User user = finder.userId(userId);

        if (!memberRepository.existsByRoomIdAndUserId(room.getId(), user.getId())) {
            throw new RuntimeException("You are not a member of this room");
        }

        ChatMessage message = chatMessageMapper.toChatMessage(request);
        message.setRoom(room);
        message.setSender(user);
        message.setContent(content);
        message.setSentAt(LocalDateTime.now());

        messageRepository.save(message);

        ChatMessageResponse response =
                chatMessageMapper.toChatMessageResponse(message);

        messagingTemplate.convertAndSend(
                "/topic/room/" + room.getId(),
                response
        );

        return response;
    }


}

