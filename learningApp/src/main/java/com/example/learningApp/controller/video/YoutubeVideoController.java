package com.example.learningApp.controller.video;

import com.example.learningApp.dto.ApiResponse;
import com.example.learningApp.dto.request.video.YoutubeVideoRequest;
import com.example.learningApp.dto.response.video.YoutubeVideoResponse;
import com.example.learningApp.dto.response.video.YoutubeVideoSummaryResponse;
import com.example.learningApp.service.video.YoutubeVideoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/youtube")
@RequiredArgsConstructor
public class YoutubeVideoController {

    private final YoutubeVideoService youtubeVideoService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<YoutubeVideoResponse>> uploadYoutubeVideo(
            @RequestBody @Valid YoutubeVideoRequest request
    ) {
        try {
            // Gọi phương thức lưu transcript luôn
            YoutubeVideoResponse videoResp = youtubeVideoService.saveYoutubeTranscriptAws(request.getUrl(),"ja-JP");
            return ResponseEntity.ok(ApiResponse.success("Video uploaded successfully with transcript", videoResp));
        } catch (Exception e) {
            int status = 500;
            return ResponseEntity
                    .status(status)
                    .body(ApiResponse.error(status, "Failed to upload video: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<YoutubeVideoSummaryResponse>>> getAllVideos() {
        List<YoutubeVideoSummaryResponse> videos = youtubeVideoService.getAllVideos();
        return ResponseEntity.ok(ApiResponse.success("All videos retrieved", videos));
    }

    /**
     * Lấy video theo ID, trả về chi tiết
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> getVideoById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Video retrieved successfully", youtubeVideoService.getVideoById(id)));
    }
}
