package com.example.learningApp.dto.response.video.comment;


import lombok.Data;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class VideoCommentResponse {

    private String id;
    private String content;
    private String userId;
    private String fullName;
    private String avatarUrl;
    private Integer userRating;
    private Instant createdAt;
    private List<VideoCommentResponse> replies;
}