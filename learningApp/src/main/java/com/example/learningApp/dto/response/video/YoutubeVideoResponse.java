package com.example.learningApp.dto.response.video;


import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // <-- thêm dòng này
public class YoutubeVideoResponse {
    private String id;                 // YouTube video ID
    private String title;
    private String description;
    private String thumbnailUrl;
    private String urlVideo;
    private String channelTitle;
    private String duration;           // PT2H35M54S
    private Instant publishedAt;
    private String s3Url;              // URL sau khi upload lên S3
    private Instant createdAt;
    private Instant updatedAt;
}

