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


    @PostMapping("/{videoId}")
    public ResponseEntity<ApiResponse<Void>> saveVideo(
            @PathVariable String videoId
    ) {
        youtubeVideoService.saveVideoForUser(videoId);
        return ResponseEntity.ok(
                ApiResponse.success("Saved video successfully", null)
        );
    }

    @DeleteMapping("/{videoId}")
    public ResponseEntity<ApiResponse<Void>> removeSavedVideo(
            @PathVariable String videoId
    ) {
        youtubeVideoService.removeSavedVideo(videoId);
        return ResponseEntity.ok(
                ApiResponse.success("Removed video successfully", null)
        );
    }
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<YoutubeVideoSummaryResponse>>> getMySavedVideos() {

        List<YoutubeVideoSummaryResponse> videos =
                youtubeVideoService.getMySavedVideos();

        return ResponseEntity.ok(
                ApiResponse.success("Get my saved videos successfully", videos)
        );
    }
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Void>> uploadYoutubeVideo(
            @RequestBody @Valid YoutubeVideoRequest request
    ) {
        try {
            youtubeVideoService.saveYoutubeTranscriptAws(request);

            return ResponseEntity.ok(
                    ApiResponse.success("Video uploaded successfully with transcript", null)
            );

        } catch (IOException | InterruptedException e) {
            int status = 500;
            return ResponseEntity
                    .status(status)
                    .body(ApiResponse.error(
                            status,
                            "Failed to upload video: " + e.getMessage()
                    ));
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
