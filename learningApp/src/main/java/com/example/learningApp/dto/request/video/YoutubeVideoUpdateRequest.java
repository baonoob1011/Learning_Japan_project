package com.example.learningApp.dto.request.video;

import com.example.learningApp.enums.JLPTLevel;
import com.example.learningApp.enums.VideoTag;
import lombok.Data;

@Data
public class YoutubeVideoUpdateRequest {
    private VideoTag videoTag;
    private JLPTLevel level;
}