package com.example.learningApp.dto.request.video;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class YoutubeTranscriptRequest {

    @NotBlank(message = "videoId không được để trống")
    private String videoId;  // video entity id

    @NotNull(message = "Danh sách transcripts không được null")
    private List<TranscriptItem> transcripts;

    @Data
    public static class TranscriptItem {
        @NotBlank(message = "Text không được để trống")
        private String text;         // toàn bộ câu

        @NotNull(message = "startOffset không được để trống")
        private Integer startOffset; // thời điểm bắt đầu câu (ms)

        @NotNull(message = "endOffset không được để trống")
        private Integer endOffset;   // thời điểm kết thúc câu (ms)

        // Tùy chọn: thêm trường phút:giây để FE hiển thị
        private String startTime;    // mm:ss
        private String endTime;      // mm:ss
    }
}

