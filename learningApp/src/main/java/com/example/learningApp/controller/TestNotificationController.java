package com.example.learningApp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TestNotificationController {

    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/test-noti/{userId}")
    public void test(@PathVariable String userId) {
        messagingTemplate.convertAndSend(
                "/topic/notifications/" + userId,
                Map.of(
                        "title", "🔥 Test Notification",
                        "content", "Nếu thấy cái này là socket OK"
                )
        );
    }
}
