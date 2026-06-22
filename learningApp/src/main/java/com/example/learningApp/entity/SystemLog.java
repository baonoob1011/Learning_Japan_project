package com.example.learningApp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_logs")
@Getter
@Setter
public class SystemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(length = 255)
    private String username;

    @Column(length = 255)
    private String userFullName;

    @Column(columnDefinition = "TEXT")
    private String userAvatar;

    @Column(length = 100)
    private String ipAddress;

    @Column(nullable = false, length = 500)
    private String targetClass;

    @Column(nullable = false, length = 255)
    private String methodName;

    @Column(columnDefinition = "TEXT")
    private String arguments;

    @Column(columnDefinition = "TEXT")
    private String result;

    @Column(nullable = false)
    private Long executionTime;

    @Column(nullable = false, length = 20)
    private String status; // SUCCESS | FAILURE

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
