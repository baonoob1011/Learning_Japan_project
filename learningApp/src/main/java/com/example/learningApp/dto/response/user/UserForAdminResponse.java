package com.example.learningApp.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserForAdminResponse {
    private String id;
    private String email;
    private String fullName;
    private Boolean enabled;
    private String avatarUrl;
    private String role;

    // --- CÁC TRƯỜNG CẦN BỔ SUNG CHO DASHBOARD ---

    private String level;          // N5, N4, N3...
    private String stage;          // Junbi, Taisaku, Exam
    private Integer processPercent; // 0 - 100
    private boolean isPremium;     // True/False

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

