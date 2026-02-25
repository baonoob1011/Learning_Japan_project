package com.example.learningApp.controller.friend;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.service.friend.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @PostMapping("/request/{receiverId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> sendRequest(@PathVariable String receiverId) {
        return ResponseEntity.ok(
                ApiResponse.success("Friend request sent", friendService.sendRequest(receiverId)));
    }

    @PostMapping("/accept/{requestId}")
    public ResponseEntity<ApiResponse<Void>> acceptRequest(@PathVariable String requestId) {
        friendService.acceptRequest(requestId);
        return ResponseEntity.ok(ApiResponse.success("Friend request accepted", null));
    }

    @PostMapping("/reject/{requestId}")
    public ResponseEntity<ApiResponse<Void>> rejectRequest(@PathVariable String requestId) {
        friendService.rejectRequest(requestId);
        return ResponseEntity.ok(ApiResponse.success("Friend request rejected", null));
    }

    @GetMapping("/status/{userId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> getStatus(@PathVariable String userId) {
        return ResponseEntity.ok(
                ApiResponse.success("Fetched habit status", friendService.getStatus(userId)));
    }
}
