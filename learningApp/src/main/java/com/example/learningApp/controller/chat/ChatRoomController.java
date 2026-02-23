package com.example.learningApp.controller.chat;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.chat.AddGroupMembersRequest;
import com.example.learningApp.dto.request.chat.CreateGroupRoomRequest;
import com.example.learningApp.dto.request.chat.CreatePrivateRoomRequest;
import com.example.learningApp.dto.response.chat.*;
import com.example.learningApp.dto.response.chat.ChatGroupDetailResponse;
import com.example.learningApp.service.chat.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat-room")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping("/private")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createPrivateRoom(
            @RequestBody CreatePrivateRoomRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Private room ready",
                        chatRoomService.getOrCreatePrivateRoom(request)
                )
        );
    }
    @PostMapping(
            value = "/group",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createGroupRoom(
            @ModelAttribute CreateGroupRoomRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Group room ready",
                        chatRoomService.createGroupRoom(request)
                )
        );
    }
    @PostMapping("/group/{roomId}/members")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> addMembers(
            @PathVariable String roomId,
            @RequestBody AddGroupMembersRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Members added successfully",
                        chatRoomService.addMembersToGroup(roomId, request)
                )
        );
    }
    @GetMapping("/my-rooms")
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getMyRooms() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "My rooms fetched successfully",
                        chatRoomService.getMyRooms()
                )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> searchMyRooms(
            @RequestParam String keyword
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Search rooms successfully",
                        chatRoomService.searchMyPrivateRooms(keyword)
                )
        );
    }

    @GetMapping("/my-users")
    public ResponseEntity<ApiResponse<List<PrivateChatPreviewResponse>>> getMyChatUsers() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Chat users fetched successfully",
                        chatRoomService.getAllOtherUsersInMyRooms()
                )
        );
    }
    @GetMapping("/my-group-rooms")
    public ResponseEntity<ApiResponse<List<ChatGroupBasicResponse>>> getAllMyGroupRooms() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "My group rooms fetched successfully",
                        chatRoomService.getAllMyGroupRooms()
                )
        );
    }

    @GetMapping("/group/{roomId}")
    public ResponseEntity<ApiResponse<ChatGroupDetailResponse>> getGroupById(
            @PathVariable String roomId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Group detail fetched successfully",
                        chatRoomService.getGroupById(roomId)
                )
        );
    }

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<ApiResponse<Page<ChatMessageResponse>>> getMessages(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Messages fetched successfully",
                        chatRoomService.getMessages(roomId, page, size)
                )
        );
    }
}
