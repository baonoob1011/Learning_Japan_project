package com.example.learningApp.dto.request.translate;

import lombok.Data;

@Data

public class TranslateRequest {
    private String videoId;
    private String text;
    private String sourceLang; // "ja"
    private String targetLang; // "vi"
}
