package com.example.learningApp.entity.auth;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_sessions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String userId; // Cognito 'sub'

    @Column(nullable = false, unique = true)
    private String sessionId;

    private String deviceInfo;
    private String ipAddress;

    private LocalDateTime createdAt;
    private LocalDateTime lastActivity;

    private boolean isActive;
}
