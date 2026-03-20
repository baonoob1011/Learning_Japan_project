package com.example.learningApp.dto.event;

import com.example.learningApp.enums.JLPTLevel;
import com.example.learningApp.enums.VideoTag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class YoutubeTranscribeMessage {
    private String videoId;
    private String url;
    private JLPTLevel level;

    private VideoTag videoTag;
}

