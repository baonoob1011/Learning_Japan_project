package com.example.learningApp.dto.request.chat;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class CreateGroupRoomRequest {
    private String name;          // 👈 thêm tên nhóm
    private MultipartFile avatar;
    private List<String> memberIds;
}

