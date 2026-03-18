package com.example.learningApp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class YoutubeTranscript {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    @JsonIgnore
    private YoutubeVideo video;

    @Column(name = "offset_sec") // thời điểm từ trong video (ms)
    private Integer offset;

    @Column(name = "translated_text", columnDefinition = "text")
    private String translatedText;

    @Column(columnDefinition = "text")
    private String text;

    @Column(name = "start_offset_ms")
    private Integer startOffset; // thời điểm bắt đầu câu (ms)

    @Column(name = "end_offset_ms")
    private Integer endOffset; // thời điểm kết thúc câu (ms)

    private LocalDateTime createdAt;
}
