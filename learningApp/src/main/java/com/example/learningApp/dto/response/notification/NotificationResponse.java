package com.example.learningApp.dto.response.notification;

import com.example.learningApp.entity.Notification;
import com.example.learningApp.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class NotificationResponse {

    private String id;
    private NotificationType type;
    private String title;
    private String content;
    private String metadata;
    private boolean isRead;
    private LocalDateTime createdAt;

    public NotificationResponse(Notification notification) {
        this.id = notification.getId();
        this.type = notification.getType();
        this.title = notification.getTitle();
        this.content = notification.getContent();
        this.metadata = notification.getMetadata();
        this.isRead = notification.isRead();
        this.createdAt = notification.getCreatedAt();
    }
}

