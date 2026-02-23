package com.example.learningApp.service.chat;
import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.request.chat.CreateGroupRoomRequest;
import com.example.learningApp.dto.request.chat.CreatePrivateRoomRequest;
import com.example.learningApp.dto.response.chat.ChatMessageResponse;
import com.example.learningApp.dto.response.chat.ChatRoomMemberResponse;
import com.example.learningApp.dto.response.chat.ChatRoomResponse;
import com.example.learningApp.dto.response.chat.ChatUserResponse;
import com.example.learningApp.entity.ChatMessage;
import com.example.learningApp.entity.ChatRoom;
import com.example.learningApp.entity.ChatRoomMember;
import com.example.learningApp.entity.User;
import com.example.learningApp.enums.RoomType;
import com.example.learningApp.mapper.ChatMessageMapper;
import com.example.learningApp.repository.ChatMessageRepository;
import com.example.learningApp.repository.ChatRoomMemberRepository;
import com.example.learningApp.repository.ChatRoomRepository;
import com.example.learningApp.service.cloud.S3Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChatRoomResponseBuilder {

    private final ChatRoomMemberRepository memberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageMapper chatMessageMapper;

    public ChatRoomResponse build(ChatRoom room, User currentUser) {

        List<ChatRoomMember> members =
                memberRepository.findByRoomId(room.getId());

        List<ChatRoomMemberResponse> memberResponses =
                members.stream()
                        .map(this::mapMember)
                        .toList();

        User otherUser = members.stream()
                .map(ChatRoomMember::getUser)
                .filter(u -> !u.getId().equals(currentUser.getId()))
                .findFirst()
                .orElse(null);

        var lastMessageOpt =
                chatMessageRepository.findTopByRoomIdOrderBySentAtDesc(room.getId());

        return ChatRoomResponse.builder()
                .id(room.getId())
                .roomType(room.getRoomType().name())
                .createdAt(room.getCreatedAt())
                .members(memberResponses)
                .name(room.getName())
                .avatarUrl(room.getAvatarUrl())
                .otherUserName(otherUser != null ? otherUser.getFullName() : null)
                .otherUserAvatar(otherUser != null ? otherUser.getAvatarUrl() : null)
                .lastMessage(lastMessageOpt.map(m -> m.getContent()).orElse(null))
                .lastMessageTime(lastMessageOpt.map(m -> m.getSentAt()).orElse(null))
                .unreadCount((int) chatMessageRepository
                        .countByRoomIdAndSenderIdNotAndIsReadFalse(
                                room.getId(),
                                currentUser.getId()
                        ))
                .build();
    }

    public ChatRoomResponse buildGroupDetail(ChatRoom room) {
        return build(room, null);
    }

    public ChatRoomMemberResponse mapMember(ChatRoomMember member) {
        return ChatRoomMemberResponse.builder()
                .userId(member.getUser().getId())
                .fullName(member.getUser().getFullName())
                .avatarUrl(member.getUser().getAvatarUrl())
                .build();
    }

    public ChatMessageResponse mapMessage(ChatMessage message) {
        return chatMessageMapper.toChatMessageResponse(message);
    }
}