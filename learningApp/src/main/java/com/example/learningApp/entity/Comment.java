package com.example.learningApp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Long timestamp; // Thời điểm comment trong video (giây)

    @Column(name = "is_private")
    private boolean isPrivate; // True = Note cá nhân, False = Comment công khai

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "video_id")
    private YoutubeVideo video;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
