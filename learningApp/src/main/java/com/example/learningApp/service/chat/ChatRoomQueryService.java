package com.example.learningApp.service.chat;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.response.chat.*;
import com.example.learningApp.dto.response.chat.ChatGroupDetailResponse;
import com.example.learningApp.entity.ChatMessage;
import com.example.learningApp.entity.ChatRoom;
import com.example.learningApp.entity.ChatRoomMember;
import com.example.learningApp.entity.User;
import com.example.learningApp.enums.RoomType;
import com.example.learningApp.repository.ChatMessageRepository;
import com.example.learningApp.repository.ChatRoomMemberRepository;
import com.example.learningApp.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatRoomQueryService {

        private final ChatRoomMemberRepository memberRepository;
        private final ChatMessageRepository chatMessageRepository;
        private final EntityFinder finder;
        private final ChatRoomResponseBuilder responseBuilder;
        private final ChatRoomRepository chatRoomRepository;

        public List<ChatRoomResponse> getMyRooms() {
                User currentUser = finder.userById();

                return memberRepository.findByUserId(currentUser.getId())
                                .stream()
                                .map(ChatRoomMember::getRoom)
                                .distinct()
                                .map(room -> responseBuilder.build(room, currentUser))
                                .sorted(this::sortByLastMessage)
                                .toList();
        }

        public List<ChatGroupBasicResponse> getMyGroupRooms() {

                User currentUser = finder.userById();

                return memberRepository.findByUserId(currentUser.getId())
                                .stream()
                                .map(ChatRoomMember::getRoom)
                                .filter(room -> room.getRoomType() == RoomType.GROUP)
                                .distinct()
                                .map(room -> {

                                        // last message
                                        var lastMessage = chatMessageRepository
                                                        .findTopByRoomIdOrderBySentAtDesc(room.getId())
                                                        .orElse(null);

                                        // unread count (tuỳ theo logic của bạn)
                                        int unreadCount = chatMessageRepository
                                                        .countUnreadMessages(room.getId(), currentUser.getId());

                                        // member count
                                        int memberCount = memberRepository.countByRoomId(room.getId());

                                        return ChatGroupBasicResponse.builder()
                                                        .id(room.getId())
                                                        .roomType(room.getRoomType().name())
                                                        .createdAt(room.getCreatedAt())
                                                        .name(room.getName())
                                                        .avatarUrl(room.getAvatarUrl())
                                                        .lastMessage(lastMessage != null ? lastMessage.getContent()
                                                                        : null)
                                                        .lastMessageTime(lastMessage != null ? lastMessage.getSentAt()
                                                                        : null)
                                                        .unreadCount(unreadCount)
                                                        .memberCount(memberCount)
                                                        .build();
                                })
                                .sorted((r1, r2) -> {
                                        if (r1.getLastMessageTime() == null)
                                                return 1;
                                        if (r2.getLastMessageTime() == null)
                                                return -1;
                                        return r2.getLastMessageTime().compareTo(r1.getLastMessageTime());
                                })
                                .toList();
        }

        public ChatGroupDetailResponse getGroupById(String roomId, String userId) {

                ChatRoom room = finder.chatRoomById(roomId);

                if (room.getRoomType() != RoomType.GROUP) {
                        throw new RuntimeException("Not a group");
                }

                if (!memberRepository.existsByRoomIdAndUserId(roomId, userId)) {
                        throw new RuntimeException("Not a member");
                }

                var members = room.getMembers()
                                .stream()
                                .map(member -> ChatGroupDetailResponse.GroupMemberInfo.builder()
                                                .userId(member.getUser().getId())
                                                .fullName(member.getUser().getFullName())
                                                .avatarUrl(member.getUser().getAvatarUrl())
                                                .build())
                                .toList();

                return ChatGroupDetailResponse.builder()
                                .id(room.getId())
                                .name(room.getName())
                                .avatarUrl(room.getAvatarUrl())
                                .createdAt(room.getCreatedAt())
                                .memberCount(members.size())
                                .members(members)
                                .build();
        }

        public Page<ChatMessageResponse> getMessages(String roomId, int page, int size) {

                User user = finder.userById();

                if (!memberRepository.existsByRoomIdAndUserId(roomId, user.getId())) {
                        throw new RuntimeException("Not a member");
                }

                Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());

                return chatMessageRepository
                                .findByRoomId(roomId, pageable)
                                .map(responseBuilder::mapMessage);
        }

        // ==============================
        // SEARCH MY PRIVATE ROOMS
        // ==============================
        public List<ChatRoomResponse> searchMyPrivateRooms(String keyword) {

                User currentUser = finder.userById();

                if (keyword == null || keyword.isBlank()) {
                        return getMyPrivateRooms(currentUser);
                }

                return memberRepository.findByUserId(currentUser.getId())
                                .stream()
                                .map(ChatRoomMember::getRoom)
                                .filter(room -> room.getRoomType() == RoomType.PRIVATE)
                                .distinct()
                                .map(room -> responseBuilder.build(room, currentUser))
                                .filter(response -> response.getOtherUserName() != null &&
                                                response.getOtherUserName()
                                                                .toLowerCase()
                                                                .contains(keyword.toLowerCase()))
                                .sorted(this::sortByLastMessage)
                                .toList();
        }

        public List<PrivateChatPreviewResponse> getAllOtherUsersInMyRooms() {

                User currentUser = finder.userById();

                return memberRepository
                                .findPrivateChatPreview(currentUser.getId())
                                .stream()
                                .map(obj -> {

                                        User user = (User) obj[0];
                                        String lastMessage = (String) obj[1];
                                        LocalDateTime lastTime = (LocalDateTime) obj[2];
                                        String roomId = (String) obj[3];

                                        return PrivateChatPreviewResponse.builder()
                                                        .roomId(roomId)
                                                        .userId(user.getId())
                                                        .fullName(user.getFullName())
                                                        .avatarUrl(user.getAvatarUrl())
                                                        .lastMessage(lastMessage)
                                                        .lastMessageTime(lastTime)
                                                        .build();
                                })
                                .toList();
        }

        private int sortByLastMessage(ChatRoomResponse r1, ChatRoomResponse r2) {
                if (r1.getLastMessageTime() == null)
                        return 1;
                if (r2.getLastMessageTime() == null)
                        return -1;
                return r2.getLastMessageTime().compareTo(r1.getLastMessageTime());
        }

        public List<ChatRoomResponse> getMyPrivateRooms(User currentUser) {

                List<ChatRoom> rooms = chatRoomRepository.findPrivateRoomsOfUser(currentUser.getId());

                return rooms.stream().map(room -> {

                        // 🔹 Lấy user còn lại trong room
                        User otherUser = room.getMembers()
                                        .stream()
                                        .map(ChatRoomMember::getUser)
                                        .filter(u -> !u.getId().equals(currentUser.getId()))
                                        .findFirst()
                                        .orElse(null);

                        // 🔹 Last message
                        ChatMessage lastMessage = room.getMessages()
                                        .stream()
                                        .max(Comparator.comparing(ChatMessage::getSentAt))
                                        .orElse(null);

                        return ChatRoomResponse.builder()
                                        .id(room.getId())
                                        .roomType(room.getRoomType().name())
                                        .createdAt(room.getCreatedAt())
                                        .name(room.getName())
                                        .avatarUrl(room.getAvatarUrl())

                                        // display info
                                        .otherUserName(otherUser != null ? otherUser.getFullName() : null)
                                        .otherUserAvatar(otherUser != null ? otherUser.getAvatarUrl() : null)

                                        // last message
                                        .lastMessage(lastMessage != null ? lastMessage.getContent() : null)
                                        .lastMessageTime(lastMessage != null ? lastMessage.getSentAt() : null)

                                        // unread count (giả sử có isRead field)
                                        .unreadCount(
                                                        (int) room.getMessages().stream()
                                                                        .filter(m -> !m.getSender().getId()
                                                                                        .equals(currentUser.getId()))
                                                                        .filter(m -> !m.getIsRead())
                                                                        .count())

                                        .build();

                }).toList();
        }
}
