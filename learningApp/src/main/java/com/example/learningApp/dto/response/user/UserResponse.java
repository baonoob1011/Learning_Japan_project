package com.example.learningApp.dto.response.user;

import com.example.learningApp.enums.JLPTLevel;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private String id;
    private String email;
    private String fullName;
    private JLPTLevel level;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String avatarUrl;
    private LocalDateTime vipExpiredAt;
    private Boolean isPremium;
    private List<String> roles;
}

