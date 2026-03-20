package com.example.learningApp.controller.video;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.video.YoutubeVideoRequest;
import com.example.learningApp.dto.request.video.YoutubeVideoUpdateRequest;
import com.example.learningApp.dto.response.video.YoutubeVideoSummaryResponse;
import com.example.learningApp.service.video.YoutubeVideoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/youtube")
@RequiredArgsConstructor
public class YoutubeVideoController {

        private final YoutubeVideoService youtubeVideoService;

        @PostMapping("/{videoId}")
        public ResponseEntity<ApiResponse<Void>> saveVideo(
                        @PathVariable String videoId) {
                youtubeVideoService.saveVideoForUser(videoId);
                return ResponseEntity.ok(
                                ApiResponse.success("Saved video successfully", null));
        }

        @PutMapping("/{videoId}")
        public ResponseEntity<ApiResponse<Void>> updateVideo(
                        @PathVariable String videoId,
                        @RequestBody YoutubeVideoUpdateRequest request) {
                youtubeVideoService.updateVideo(videoId, request);

                return ResponseEntity.ok(
                                ApiResponse.success("Video updated successfully", null));
        }

        @DeleteMapping("/{videoId}")
        public ResponseEntity<ApiResponse<Void>> removeSavedVideo(
                        @PathVariable String videoId) {
                youtubeVideoService.removeSavedVideo(videoId);
                return ResponseEntity.ok(
                                ApiResponse.success("Removed video successfully", null));
        }

        @GetMapping("/me")
        public ResponseEntity<ApiResponse<List<YoutubeVideoSummaryResponse>>> getMySavedVideos() {

                List<YoutubeVideoSummaryResponse> videos = youtubeVideoService.getMySavedVideos();

                return ResponseEntity.ok(
                                ApiResponse.success("Get my saved videos successfully", videos));
        }

        @PostMapping("/upload")
        public ResponseEntity<ApiResponse<Void>> uploadYoutubeVideo(
                        @RequestBody @Valid YoutubeVideoRequest request) {
                youtubeVideoService.saveYoutubeTranscriptAws(request);

                return ResponseEntity.ok(
                                ApiResponse.success("Video uploaded successfully with transcript", null));

        }

        @GetMapping
        public ResponseEntity<ApiResponse<List<YoutubeVideoSummaryResponse>>> getAllVideos() {
                List<YoutubeVideoSummaryResponse> videos = youtubeVideoService.getAllVideos();
                return ResponseEntity.ok(ApiResponse.success("All videos retrieved", videos));
        }

        @GetMapping("/vocab")
        public ResponseEntity<ApiResponse<List<YoutubeVideoSummaryResponse>>> getAllVideoByVocab() {
                List<YoutubeVideoSummaryResponse> videos = youtubeVideoService.getAllVideoByVocab();
                return ResponseEntity.ok(ApiResponse.success("All videos by vocab", videos));
        }

        /**
         * Lấy video theo ID, trả về chi tiết
         */
        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<Void>> getVideoById(@PathVariable String id) {
                return ResponseEntity.ok(ApiResponse.success("Video retrieved successfully",
                                youtubeVideoService.getVideoById(id)));
        }
}

