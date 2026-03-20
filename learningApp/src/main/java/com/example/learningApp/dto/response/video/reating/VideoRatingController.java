package com.example.learningApp.dto.response.video.reating;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.video.rating.RateVideoRequest;
import com.example.learningApp.dto.response.video.rating.VideoRatingResponse;
import com.example.learningApp.service.video.rating.VideoRatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/youtube/ratings")
@RequiredArgsConstructor
public class VideoRatingController {

    private final VideoRatingService videoRatingService;

    /**
     * Đánh giá hoặc cập nhật đánh giá video (1-5 sao)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> rateVideo(
            @RequestBody @Valid RateVideoRequest request
    ) {

        videoRatingService.rateVideo(request);

        return ResponseEntity.ok(
                ApiResponse.success("Video rated successfully", null)
        );
    }

    /**
     * Lấy rating trung bình và tổng số lượt đánh giá của video
     */
    @GetMapping("/{videoId}")
    public ResponseEntity<ApiResponse<VideoRatingResponse>> getVideoRating(
            @PathVariable String videoId
    ) {

        VideoRatingResponse response =
                videoRatingService.getVideoRating(videoId);

        return ResponseEntity.ok(
                ApiResponse.success("Video rating retrieved successfully", response)
        );
    }

    /**
     * Xóa rating của user hiện tại cho video
     */
    @DeleteMapping("/{videoId}")
    public ResponseEntity<ApiResponse<Void>> deleteMyRating(
            @PathVariable String videoId
    ) {

        videoRatingService.deleteMyRating(videoId);

        return ResponseEntity.ok(
                ApiResponse.success("Rating removed successfully", null)
        );
    }
}
