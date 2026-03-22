package com.example.learningApp.controller;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.listener.WebSocketEventListener;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/users/online")
@RequiredArgsConstructor
@Tag(name = "User Online status", description = "Monitor online users")
public class UserOnlineController {

    @GetMapping
    @Operation(summary = "Get list of online user IDs (current WebSocket sessions)")
    public ResponseEntity<ApiResponse<Set<String>>> getOnlineUserIds() {
        Set<String> onlineUsers = WebSocketEventListener.getOnlineUsers();
        return ResponseEntity.ok(ApiResponse.success("Online users fetched", onlineUsers));
    }
}
