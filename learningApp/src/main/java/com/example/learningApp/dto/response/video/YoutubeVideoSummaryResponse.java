package com.example.learningApp.dto.response.video;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class YoutubeVideoSummaryResponse {
    private String id;
    private String title;
    private String s3Url;
    private String duration;           // PT2H35M54S
    private Instant createdAt;
}
