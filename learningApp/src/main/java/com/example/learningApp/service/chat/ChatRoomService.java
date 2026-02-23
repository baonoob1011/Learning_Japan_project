package com.example.learningApp.service.chat;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.request.chat.AddGroupMembersRequest;
import com.example.learningApp.dto.request.chat.CreateGroupRoomRequest;
import com.example.learningApp.dto.request.chat.CreatePrivateRoomRequest;
import com.example.learningApp.dto.response.chat.*;
import com.example.learningApp.dto.response.chat.ChatGroupDetailResponse;
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
public class ChatRoomService {

    private final ChatRoomQueryService queryService;
    private final ChatRoomCommandService commandService;
    private final EntityFinder finder;

    public List<ChatRoomResponse> getMyRooms() {
        return queryService.getMyRooms();
    }

    public List<ChatGroupBasicResponse> getAllMyGroupRooms() {
        return queryService.getMyGroupRooms();
    }

    public ChatGroupDetailResponse getGroupById(String roomId) {
        User currentUser = finder.userById();

        return queryService.getGroupById(roomId,currentUser.getId());
    }

    public Page<ChatMessageResponse> getMessages(String roomId, int page, int size) {
        return queryService.getMessages(roomId, page, size);
    }

    public ChatRoomResponse getOrCreatePrivateRoom(CreatePrivateRoomRequest request) {
        return commandService.getOrCreatePrivateRoom(request);
    }

    public ChatRoomResponse addMembersToGroup(String roomId, AddGroupMembersRequest request) {
        return commandService.addMembersToGroup(roomId,request);
    }

    public ChatRoomResponse createGroupRoom(CreateGroupRoomRequest request) {
        return commandService.createGroupRoom(request);
    }

    public List<ChatRoomResponse> searchMyPrivateRooms(String keyword) {
        return queryService.searchMyPrivateRooms(keyword);
    }

    public List<PrivateChatPreviewResponse> getAllOtherUsersInMyRooms() {
        return queryService.getAllOtherUsersInMyRooms();
    }
}