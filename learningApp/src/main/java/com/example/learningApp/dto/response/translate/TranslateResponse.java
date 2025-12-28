package com.example.learningApp.dto.response.translate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TranslateResponse {

    private String videoId;
    private String surface;
    private String translated;
    private String reading;
    private String romaji;
    private String partOfSpeech;  // loại từ
    private String targetDefs;    // nghĩa ngôn ngữ đích
    private String audioUrl;   // audio lưu đường dẫn s3
    private String explain;   // audio lưu đường dẫn s3

}
