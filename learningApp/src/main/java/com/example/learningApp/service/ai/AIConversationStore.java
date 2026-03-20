package com.example.learningApp.service.ai;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AIConversationStore {

    private final Map<String, List<Map<String, String>>> history = new ConcurrentHashMap<>();

    public void addMessage(String sessionId, String role, String content) {
        history.computeIfAbsent(sessionId, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(Map.of("role", role, "content", content));
    }

    public List<Map<String, String>> getHistory(String sessionId) {
        return history.getOrDefault(sessionId, new ArrayList<>());
    }

    public void clear(String sessionId) {
        history.remove(sessionId);
    }
}

