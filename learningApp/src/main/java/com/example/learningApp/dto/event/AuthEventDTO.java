package com.example.learningApp.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthEventDTO implements Serializable {
    private String userId;
    private String email;
    private String fullName;
    private String action; // e.g., "REGISTER", "LOGIN"
    private String timestamp;
}
