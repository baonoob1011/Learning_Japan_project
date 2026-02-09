package com.example.learningApp.dto.response.notìication;

import com.example.learningApp.entity.Notification;
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
    private String title;
    private String content;
    private  boolean isRead;
    private LocalDateTime createdAt;

    public NotificationResponse(Notification notification) {
    }
}
