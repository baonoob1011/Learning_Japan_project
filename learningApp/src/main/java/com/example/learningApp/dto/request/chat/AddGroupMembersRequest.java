package com.example.learningApp.dto.request.chat;

import lombok.Data;

import java.util.Set;

@Data
public class AddGroupMembersRequest {
    private Set<String> memberIds;
}