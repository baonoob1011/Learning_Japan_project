package com.example.learningApp.controller.auth;

import com.example.learningApp.service.auth.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/session")
@RequiredArgsConstructor
public class SessionController {
    private final SessionService sessionService;

    /**
     * Initializes a new session AFTER Cognito login.
     * Client would call this with its Access Token to get the unique sessionId.
     */
    @PostMapping("/init")
    public Map<String, String> initSession(Authentication auth, HttpServletRequest request) {
        String userId = auth.getName(); // Cognito 'sub'
        String deviceInfo = request.getHeader("User-Agent");
        String ipAddress = request.getRemoteAddr();

        String sessionId = sessionService.initNewSession(userId, deviceInfo, ipAddress);
        return Map.of("sessionId", sessionId);
    }

    @PostMapping("/logout")
    public Map<String, String> logout(Authentication auth) {
        String userId = auth.getName();
        sessionService.logout(userId);
        return Map.of("message", "Logged out successfully.");
    }
}
