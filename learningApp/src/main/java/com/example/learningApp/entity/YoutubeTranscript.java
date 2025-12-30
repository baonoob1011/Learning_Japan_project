package com.example.learningApp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YoutubeTranscript {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private YoutubeVideo video;

    @Column(name = "offset_sec") // thời điểm từ trong video (ms)
    private Integer offset;

    @Column(columnDefinition = "text")
    private String text;

    private String audioUrl;

    @Column(name = "start_offset_ms")
    private Integer startOffset; // thời điểm bắt đầu câu (ms)

    @Column(name = "end_offset_ms")
    private Integer endOffset;   // thời điểm kết thúc câu (ms)

    private LocalDateTime createdAt;
}
