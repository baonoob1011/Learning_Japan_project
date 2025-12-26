package com.example.learningApp.dto.request.vode;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class YoutubeVideoRequest {
    @NotBlank(message = "YouTube URL is required")
    private String url;
}
