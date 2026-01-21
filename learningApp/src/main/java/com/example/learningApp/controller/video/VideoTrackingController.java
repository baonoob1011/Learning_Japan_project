package com.example.learningApp.controller.video;


import com.example.learningApp.dto.ApiResponse;
import com.example.learningApp.dto.request.video.VideoProgressRequest;
import com.example.learningApp.service.video.UserVideoTrackingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/video-tracking")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VideoTrackingController {

    UserVideoTrackingService videoTrackingService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> saveVideoProgress(
            @RequestBody VideoProgressRequest request
    ) {
        videoTrackingService.saveUserVideoTracking(request);
        return ResponseEntity.ok(
                ApiResponse.success("Save video progress success", null)
        );
    }
}
