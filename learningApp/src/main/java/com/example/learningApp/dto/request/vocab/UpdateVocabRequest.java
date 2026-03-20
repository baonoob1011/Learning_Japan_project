package com.example.learningApp.dto.request.vocab;


import lombok.Data;

@Data
public class UpdateVocabRequest {
    private String surface;      // vocab cần sửa
    private String translated;   // nghĩa chính
}

