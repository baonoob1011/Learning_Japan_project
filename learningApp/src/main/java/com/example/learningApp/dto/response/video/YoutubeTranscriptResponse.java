package com.example.learningApp.dto.response.video;


import com.example.learningApp.entity.YoutubeVideo;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YoutubeTranscriptResponse {
    private String id;
    private String videoId;       // video liên kết
    private String urlVideo;
    private String title;
    List<transcriptsDTO> transcriptsDTOS;
    // video liên kết
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class transcriptsDTO {
        private String id;
        private String text;              // toàn bộ câu
        private Integer startOffset; // giờ phút: giây (mm:ss)
        private String translatedText;
        private Integer endOffset;   // giờ phút: giây (mm:ss)   // thời điểm kết thúc câu (ms)
        private LocalDateTime createdAt;
    }
}