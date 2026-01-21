package com.example.learningApp.controller.video;

import com.example.learningApp.dto.ApiResponse;
import com.example.learningApp.dto.request.video.VideoProgressRequest;
import com.example.learningApp.dto.response.video.VideoProgressResponse;
import com.example.learningApp.service.video.UserVideoTrackingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/video-tracking")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VideoTrackingController {

    UserVideoTrackingService videoTrackingService;

    /**
     * Save / update video progress
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> saveVideoProgress(
            @RequestBody VideoProgressRequest request
    ) {
        videoTrackingService.saveUserVideoTracking(request);

        return ResponseEntity.ok(
                ApiResponse.success("Save video progress success", null)
        );
    }

    /**
     * Get all video progress of current user
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<VideoProgressResponse>>> getAllVideoProgress() {

        List<VideoProgressResponse> progress =
                videoTrackingService.getAllUserVideoProgress();

        return ResponseEntity.ok(
                ApiResponse.success("Get all video progress success", progress)
        );
    }
}
