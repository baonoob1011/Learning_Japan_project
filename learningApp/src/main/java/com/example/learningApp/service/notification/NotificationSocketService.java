package com.example.learningApp.service.notification;

import com.example.learningApp.dto.response.notìication.NotificationResponse;
import com.example.learningApp.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationSocketService {

        private final SimpMessagingTemplate messagingTemplate;

        public void push(String userId, Notification n) {

                NotificationResponse dto = new NotificationResponse(
                                n.getId(),
                                n.getTitle(),
                                n.getContent(),
                                n.isRead(),
                                n.getCreatedAt());

                System.out.println("📤 Pushing notification to topic: /topic/notifications/" + userId);
                messagingTemplate.convertAndSend(
                                "/topic/notifications/" + userId,
                                dto);

        }
}
