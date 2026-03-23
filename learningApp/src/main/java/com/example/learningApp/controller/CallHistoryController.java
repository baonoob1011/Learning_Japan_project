package com.example.learningApp.controller;

import com.example.learningApp.entity.CallHistory;
import com.example.learningApp.service.call.CallHistoryService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/call-history")
@RequiredArgsConstructor
public class CallHistoryController {

    private final CallHistoryService callHistoryService;

    @PostMapping("/save")
    public ResponseEntity<CallHistory> saveCall(@RequestBody CallRecordRequest request) {
        CallHistory saved = callHistoryService.saveCallRecord(
                request.getCallerId(),
                request.getReceiverId(),
                request.getType(),
                request.getStatus(),
                request.getDuration(),
                request.getRoomId()
        );
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CallHistory>> getUserHistory(@PathVariable String userId) {
        return ResponseEntity.ok(callHistoryService.getCallHistoryForUser(userId));
    }

    @Data
    public static class CallRecordRequest {
        private String callerId;
        private String receiverId;
        private String type; // VIDEO or VOICE
        private String status; // COMPLETED, MISSED, REJECTED, CANCELLED
        private Integer duration;
        private String roomId;
    }
}

