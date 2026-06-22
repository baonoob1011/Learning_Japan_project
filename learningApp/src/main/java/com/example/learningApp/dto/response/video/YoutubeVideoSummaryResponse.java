package com.example.learningApp.dto.response.video;


import com.example.learningApp.enums.JLPTLevel;
import com.example.learningApp.enums.VideoTag;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class YoutubeVideoSummaryResponse {
    private String id;
    private String title;
    private String urlVideo;
    private VideoTag videoTag;
    private JLPTLevel level;
    private String duration;           // PT2H35M54S
    private Instant createdAt;
}

