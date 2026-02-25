package com.example.learningApp.dto.response.chat;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ChatGroupDetailResponse {

    private String id;
    private String name;
    private String avatarUrl;

    private LocalDateTime createdAt;

    private int memberCount;

    private List<GroupMemberInfo> members;

    @Data
    @Builder
    public static class GroupMemberInfo {
        private String userId;
        private String fullName;
        private String avatarUrl;
    }
}