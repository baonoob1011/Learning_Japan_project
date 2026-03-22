package com.example.learningApp.service.auth;

import com.example.learningApp.entity.auth.UserSession;
import com.example.learningApp.repository.auth.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {
    private final StringRedisTemplate redisTemplate;
    private final UserSessionRepository userSessionRepository;

    private static final String REDIS_KEY_PREFIX = "active_session:";

    /**
     * Initializes a new session for a user.
     * Overwrites any existing session in Redis to force logout other devices.
     */
    @Transactional
    public String initNewSession(String userId, String deviceInfo, String ipAddress) {
        String newSessionId = UUID.randomUUID().toString();
        String redisKey = REDIS_KEY_PREFIX + userId;

        // 1. Invalidate old sessions in Database
        userSessionRepository.deactivateAllSessionsByUserId(userId);

        // 2. Save new session to Database
        UserSession session = UserSession.builder()
                .userId(userId)
                .sessionId(newSessionId)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .createdAt(LocalDateTime.now())
                .lastActivity(LocalDateTime.now())
                .isActive(true)
                .build();
        userSessionRepository.save(session);

        // 3. Store in Redis with TTL (1 hour to match Cognito Access Token)
        redisTemplate.opsForValue().set(redisKey, newSessionId, Duration.ofHours(1));

        log.info("[AUTH_SESSION] User {} Logged in from device {}. Session ID: {}", userId, deviceInfo, newSessionId);
        return newSessionId;
    }

    public boolean isSessionValid(String userId, String providedSessionId) {
        if (userId == null || providedSessionId == null)
            return false;
        String redisKey = REDIS_KEY_PREFIX + userId;
        String activeSessionId = redisTemplate.opsForValue().get(redisKey);
        return providedSessionId.equals(activeSessionId);
    }

    public void logout(String userId) {
        String redisKey = REDIS_KEY_PREFIX + userId;
        redisTemplate.delete(redisKey);
        userSessionRepository.deactivateAllSessionsByUserId(userId);
    }
}
