package com.example.learningApp.controller.friend;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.friend.SendFriendRequest;
import com.example.learningApp.dto.request.friend.FriendActionRequest;
import com.example.learningApp.dto.response.friend.FriendRequestResponse;
import com.example.learningApp.service.friend.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    // =========================
    // SEND FRIEND REQUEST
    // =========================
    @PostMapping("/request/{receiverId}")
    public ResponseEntity<ApiResponse<FriendRequestResponse>> sendRequest(
            @PathVariable String receiverId) {

        SendFriendRequest dto = new SendFriendRequest();
        dto.setReceiverId(receiverId);

        FriendRequestResponse response = friendService.sendRequest(dto);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Friend request sent",
                        response
                )
        );
    }
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<FriendRequestResponse>>> getPendingRequests() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Pending friend requests",
                        friendService.getPendingRequests()
                )
        );
    }
    // =========================
    // ACCEPT REQUEST
    // =========================
    @PostMapping("/accept/{requestId}")
    public ResponseEntity<ApiResponse<Void>> acceptRequest(
            @PathVariable String requestId) {

        FriendActionRequest dto = new FriendActionRequest();
        dto.setRequestId(requestId);

        friendService.acceptRequest(dto);

        return ResponseEntity.ok(
                ApiResponse.success("Friend request accepted", null)
        );
    }

    // =========================
    // REJECT REQUEST
    // =========================
    @PostMapping("/reject/{requestId}")
    public ResponseEntity<ApiResponse<Void>> rejectRequest(
            @PathVariable String requestId) {

        FriendActionRequest dto = new FriendActionRequest();
        dto.setRequestId(requestId);

        friendService.rejectRequest(dto);

        return ResponseEntity.ok(
                ApiResponse.success("Friend request rejected", null)
        );
    }

    // =========================
    // CHECK STATUS
    // =========================
    @GetMapping("/status/{userId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> getStatus(
            @PathVariable String userId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Friend status fetched",
                        friendService.getStatus(userId)
                )
        );
    }
}