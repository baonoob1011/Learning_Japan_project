package com.example.learningApp.entity;
import com.example.learningApp.enums.AssessmentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exercises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private YoutubeVideo video;

    private String title; // Ví dụ: "Luyện nghe video ABC"

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer totalQuestions;

    private Instant createdAt;
}