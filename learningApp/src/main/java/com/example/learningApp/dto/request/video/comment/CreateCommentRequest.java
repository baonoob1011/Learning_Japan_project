package com.example.learningApp.dto.request.video.comment;


import lombok.Data;

@Data
public class CreateCommentRequest {
    private String videoId;
    private String content;
    private String parentId; // null nếu comment thường
    private Integer rating; // optional (1-5)

}