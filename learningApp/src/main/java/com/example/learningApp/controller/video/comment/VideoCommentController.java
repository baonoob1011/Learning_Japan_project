package com.example.learningApp.controller.video.comment;


import com.example.learningApp.common.ApiResponse;

import com.example.learningApp.dto.request.video.comment.CreateCommentRequest;
import com.example.learningApp.dto.response.video.comment.VideoCommentResponse;
import com.example.learningApp.service.video.comment.VideoCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/youtube/comments")
@RequiredArgsConstructor
public class VideoCommentController {

    private final VideoCommentService videoCommentService;

    /**
     * Tạo comment hoặc reply
     */
    @PostMapping
    public ResponseEntity<ApiResponse<VideoCommentResponse>> createComment(
            @RequestBody @Valid CreateCommentRequest request
    ) {

        VideoCommentResponse response =
                videoCommentService.createComment(request);

        return ResponseEntity.ok(
                ApiResponse.success("Comment created successfully", response)
        );
    }

    /**
     * Lấy toàn bộ comment theo video (bao gồm replies)
     */
    @GetMapping("/{videoId}")
    public ResponseEntity<ApiResponse<List<VideoCommentResponse>>> getComments(
            @PathVariable String videoId
    ) {

        List<VideoCommentResponse> responses =
                videoCommentService.getComments(videoId);

        return ResponseEntity.ok(
                ApiResponse.success("Comments retrieved successfully", responses)
        );
    }


}
