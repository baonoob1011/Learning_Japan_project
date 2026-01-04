package com.example.learningApp.controller.transcrip;


import com.example.learningApp.dto.ApiResponse;
import com.example.learningApp.dto.response.video.YoutubeTranscriptResponse;
import com.example.learningApp.service.video.TranscriptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transcripts")
@RequiredArgsConstructor
public class TranscriptController {

    private final TranscriptService transcriptService;

    /**
     * Lấy transcript theo videoId, sắp xếp theo thời gian, hiển thị phút:giây
     */
    @GetMapping("/{videoId}")
    public ResponseEntity<ApiResponse<YoutubeTranscriptResponse>> getTranscripts(@PathVariable String videoId) {
        // Gọi phương thức lưu transcript luôn
        return ResponseEntity.ok(ApiResponse.success("Video uploaded successfully with transcript", transcriptService.getTranscriptsByVideoId(videoId)));
    }
}
