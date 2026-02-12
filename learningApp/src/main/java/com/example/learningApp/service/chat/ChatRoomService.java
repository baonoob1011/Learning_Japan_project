package com.example.learningApp.service.chat;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.request.chat.CreatePrivateRoomRequest;
import com.example.learningApp.dto.response.chat.ChatMessageResponse;
import com.example.learningApp.dto.response.chat.ChatRoomResponse;
import com.example.learningApp.entity.ChatRoom;
import com.example.learningApp.entity.ChatRoomMember;
import com.example.learningApp.entity.User;
import com.example.learningApp.enums.RoomType;
import com.example.learningApp.mapper.ChatMessageMapper;
import com.example.learningApp.repository.ChatMessageRepository;
import com.example.learningApp.repository.ChatRoomMemberRepository;
import com.example.learningApp.repository.ChatRoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository memberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageMapper chatMessageMapper;
    private final EntityFinder finder;

    // ==============================
    // CREATE OR GET PRIVATE ROOM
    // ==============================
    @Transactional
    public ChatRoomResponse getOrCreatePrivateRoom(CreatePrivateRoomRequest request) {

        User currentUser = finder.userById();
        User targetUser = finder.userId(request.getTargetUserId());

        if (currentUser.getId().equals(targetUser.getId())) {
            throw new RuntimeException("Cannot create private room with yourself");
        }

        String privateKey = generatePrivateKey(
                currentUser.getId(),
                targetUser.getId()
        );

        Optional<ChatRoom> existingRoom =
                chatRoomRepository.findByPrivateKey(privateKey);

        if (existingRoom.isPresent()) {
            return buildResponse(existingRoom.get(), currentUser);
        }

        ChatRoom newRoom = ChatRoom.builder()
                .roomType(RoomType.PRIVATE)
                .privateKey(privateKey)
                .createdAt(LocalDateTime.now())
                .build();

        chatRoomRepository.save(newRoom);

        memberRepository.save(
                ChatRoomMember.builder()
                        .room(newRoom)
                        .user(currentUser)
                        .joinedAt(LocalDateTime.now())
                        .isAdmin(false)
                        .isMuted(false)
                        .build()
        );

        memberRepository.save(
                ChatRoomMember.builder()
                        .room(newRoom)
                        .user(targetUser)
                        .joinedAt(LocalDateTime.now())
                        .isAdmin(false)
                        .isMuted(false)
                        .build()
        );

        return buildResponse(newRoom, currentUser);
    }

    // ==============================
    // GET MY ROOMS (Messenger style)
    // ==============================
    public List<ChatRoomResponse> getMyRooms() {

        User currentUser = finder.userById();

        List<ChatRoomMember> memberships =
                memberRepository.findByUserId(currentUser.getId());

        return memberships.stream()
                .map(ChatRoomMember::getRoom)
                .distinct()
                .map(room -> buildResponse(room, currentUser))
                .sorted((r1, r2) -> {
                    if (r1.getLastMessageTime() == null) return 1;
                    if (r2.getLastMessageTime() == null) return -1;
                    return r2.getLastMessageTime()
                            .compareTo(r1.getLastMessageTime());
                })
                .toList();
    }

    // ==============================
    // GET MESSAGES
    // ==============================
    public Page<ChatMessageResponse> getMessages(
            String roomId,
            int page,
            int size
    ) {

        ChatRoom room = finder.chatRoomById(roomId);
        User user = finder.userById();

        if (!memberRepository.existsByRoomIdAndUserId(roomId, user.getId())) {
            throw new RuntimeException("You are not a member of this room");
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("sentAt").descending()
        );

        return chatMessageRepository
                .findByRoomId(roomId, pageable)
                .map(chatMessageMapper::toChatMessageResponse);
    }

    // ==============================
    // BUILD RESPONSE (FULL DATA)
    // ==============================
    private ChatRoomResponse buildResponse(ChatRoom room, User currentUser) {

        List<ChatRoomMember> members =
                memberRepository.findByRoomId(room.getId());

        List<String> memberIds = members.stream()
                .map(member -> member.getUser().getId())
                .toList();

        User otherUser = members.stream()
                .map(ChatRoomMember::getUser)
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .findFirst()
                .orElse(null);

        var lastMessageOpt =
                chatMessageRepository
                        .findTopByRoomIdOrderBySentAtDesc(room.getId());

        String lastMessage = null;
        LocalDateTime lastMessageTime = null;

        if (lastMessageOpt.isPresent()) {
            lastMessage = lastMessageOpt.get().getContent();
            lastMessageTime = lastMessageOpt.get().getSentAt();
        }

        long unreadCount =
                chatMessageRepository
                        .countByRoomIdAndSenderIdNotAndIsReadFalse(
                                room.getId(),
                                currentUser.getId()
                        );

        return ChatRoomResponse.builder()
                .id(room.getId())
                .roomType(room.getRoomType().name())
                .createdAt(room.getCreatedAt())
                .memberIds(memberIds)
                .otherUserName(otherUser != null ? otherUser.getFullName() : null)
                .otherUserAvatar(otherUser != null ? otherUser.getAvatarUrl() : null)
                .lastMessage(lastMessage)
                .lastMessageTime(lastMessageTime)
                .unreadCount((int) unreadCount)
                .build();
    }

    // ==============================
    // PRIVATE KEY GENERATOR
    // ==============================
    private String generatePrivateKey(String user1, String user2) {
        return user1.compareTo(user2) < 0
                ? user1 + "_" + user2
                : user2 + "_" + user1;
    }
}
