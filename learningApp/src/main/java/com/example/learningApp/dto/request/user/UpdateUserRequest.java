package com.example.learningApp.dto.request.user;

import com.example.learningApp.enums.JLPTLevel;
import lombok.Data;

@Data
public class UpdateUserRequest {

    private String fullName;

    private JLPTLevel level;

}