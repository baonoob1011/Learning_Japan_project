package com.example.learningApp.dto.request.vocab;

import lombok.Data;

@Data
public class UpdateVocabRequest {
    private String surface; // vocab cần tìm

    // Personal customizations
    private String customTranslated; // nghĩa hiển thị riêng cho user
    private String personalNote; // ghi chú riêng
    private String personalExample; // ví dụ riêng
    private java.util.Set<String> personalTags; // tag riêng
    private com.example.learningApp.enums.LearningStatus status; // trạng thái học tập mới
}
