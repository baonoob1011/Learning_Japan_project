package com.example.learningApp.controller.chat;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.chat.CreatePrivateRoomRequest;
import com.example.learningApp.dto.response.chat.ChatMessageResponse;
import com.example.learningApp.dto.response.chat.ChatRoomResponse;
import com.example.learningApp.service.chat.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    @GetMapping("/my-rooms")
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getMyRooms() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "My rooms fetched successfully",
                        chatRoomService.getMyRooms()
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
