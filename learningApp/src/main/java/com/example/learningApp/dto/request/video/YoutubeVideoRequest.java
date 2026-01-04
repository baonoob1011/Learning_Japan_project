package com.example.learningApp.dto.request.video;


import com.example.learningApp.enums.JLPTLevel;
import com.example.learningApp.enums.VideoTag;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class YoutubeVideoRequest {
    @NotBlank(message = "YouTube URL is required")
    private String url;
    private VideoTag videoTag;
    private JLPTLevel level;
}
