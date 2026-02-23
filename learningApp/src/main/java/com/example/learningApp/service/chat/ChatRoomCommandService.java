package com.example.learningApp.service.chat;
import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.request.chat.AddGroupMembersRequest;
import com.example.learningApp.dto.request.chat.CreateGroupRoomRequest;
import com.example.learningApp.dto.request.chat.CreatePrivateRoomRequest;
import com.example.learningApp.dto.response.chat.ChatMessageResponse;
import com.example.learningApp.dto.response.chat.ChatRoomMemberResponse;
import com.example.learningApp.dto.response.chat.ChatRoomResponse;
import com.example.learningApp.dto.response.chat.ChatUserResponse;
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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomCommandService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository memberRepository;
    private final EntityFinder finder;
    private final S3Service s3Service;
    private final ChatRoomResponseBuilder responseBuilder;

    public ChatRoomResponse getOrCreatePrivateRoom(CreatePrivateRoomRequest request) {

        User currentUser = finder.userById();
        User targetUser = finder.userId(request.getTargetUserId());

        if (currentUser.getId().equals(targetUser.getId())) {
            throw new RuntimeException("Cannot chat with yourself");
        }

        String privateKey = generatePrivateKey(currentUser.getId(), targetUser.getId());

        return chatRoomRepository.findByPrivateKey(privateKey)
                .map(room -> responseBuilder.build(room, currentUser))
                .orElseGet(() -> createPrivateRoom(currentUser, targetUser, privateKey));
    }

    private ChatRoomResponse createPrivateRoom(User u1, User u2, String key) {

        ChatRoom room = ChatRoom.builder()
                .roomType(RoomType.PRIVATE)
                .privateKey(key)
                .createdAt(LocalDateTime.now())
                .build();

        chatRoomRepository.save(room);

        saveMember(room, u1, false);
        saveMember(room, u2, false);

        return responseBuilder.build(room, u1);
    }
    public ChatRoomResponse addMembersToGroup(String roomId, AddGroupMembersRequest request) {

        User currentUser = finder.userById();

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (room.getRoomType() != RoomType.GROUP) {
            throw new RuntimeException("Not a group room");
        }

        // check quyền admin
        ChatRoomMember currentMember = memberRepository
                .findByRoomIdAndUserId(roomId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("You are not in this group"));

        if (!Boolean.TRUE.equals(currentMember.getIsAdmin())) {
            throw new RuntimeException("Only admin can add members");
        }

        if (request.getMemberIds() == null || request.getMemberIds().isEmpty()) {
            throw new RuntimeException("Member list required");
        }

        // Lấy danh sách member hiện tại
        Set<String> existingMemberIds = memberRepository
                .findByRoomId(roomId)
                .stream()
                .map(m -> m.getUser().getId())
                .collect(Collectors.toSet());

        for (String userId : request.getMemberIds()) {

            if (existingMemberIds.contains(userId)) {
                continue; // đã là member rồi thì bỏ qua
            }

            User user = finder.userId(userId);

            memberRepository.save(
                    ChatRoomMember.builder()
                            .room(room)
                            .user(user)
                            .joinedAt(LocalDateTime.now())
                            .isAdmin(false)
                            .isMuted(false)
                            .build()
            );
        }

        return responseBuilder.build(room, currentUser);
    }
    public ChatRoomResponse createGroupRoom(CreateGroupRoomRequest request) {

        User currentUser = finder.userById();

        if (request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("Group name required");
        }

        Set<String> memberIds = new HashSet<>();
        memberIds.add(currentUser.getId());
        memberIds.addAll(request.getMemberIds());

        if (memberIds.size() < 2) {
            throw new RuntimeException("Minimum 2 members");
        }

        String avatarUrl = uploadAvatarIfExists(request);

        ChatRoom room = ChatRoom.builder()
                .roomType(RoomType.GROUP)
                .name(request.getName().trim())
                .avatarUrl(avatarUrl)
                .createdAt(LocalDateTime.now())
                .build();

        chatRoomRepository.save(room);

        memberIds.forEach(id ->
                saveMember(room, finder.userId(id), id.equals(currentUser.getId()))
        );

        return responseBuilder.build(room, currentUser);
    }

    private void saveMember(ChatRoom room, User user, boolean isAdmin) {
        memberRepository.save(
                ChatRoomMember.builder()
                        .room(room)
                        .user(user)
                        .joinedAt(LocalDateTime.now())
                        .isAdmin(isAdmin)
                        .isMuted(false)
                        .build()
        );
    }

    private String uploadAvatarIfExists(CreateGroupRoomRequest request) {
        try {
            if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
                return s3Service.uploadFile(request.getAvatar(), "chat/group-avatar");
            }
        } catch (Exception e) {
            throw new RuntimeException("Upload failed");
        }
        return null;
    }

    private String generatePrivateKey(String u1, String u2) {
        return u1.compareTo(u2) < 0 ? u1 + "_" + u2 : u2 + "_" + u1;
    }
}