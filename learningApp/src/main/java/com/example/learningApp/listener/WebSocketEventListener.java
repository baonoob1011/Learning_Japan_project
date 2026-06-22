package com.example.learningApp.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    // Sử dụng ConcurrentHashMap để đảm bảo thread-safe
    private static final Set<String> onlineUsers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = Optional.ofNullable(headerAccessor.getUser())
                .map(java.security.Principal::getName)
                .orElse(null);

        if (username != null) {
            onlineUsers.add(username);
            log.info("[WS_ONLINE] User Connected: {}", username);
            broadcastOnlineUsers();
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = Optional.ofNullable(headerAccessor.getUser())
                .map(java.security.Principal::getName)
                .orElse(null);

        if (username != null) {
            onlineUsers.remove(username);
            log.info("[WS_OFFLINE] User Disconnected: {}", username);
            broadcastOnlineUsers();
        }
    }

    private void broadcastOnlineUsers() {
        messagingTemplate.convertAndSend("/topic/online-users", onlineUsers);
    }

    public static Set<String> getOnlineUsers() {
        return new HashSet<>(onlineUsers);
    }
}
