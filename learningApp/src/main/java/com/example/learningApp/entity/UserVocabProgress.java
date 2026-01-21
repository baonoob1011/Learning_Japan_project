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
public class UserVocabProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // ===== User =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ===== Vocab =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocab_id", nullable = false)
    private Vocab vocab;

    // ===== Progress =====
    @Builder.Default
    @Column(nullable = false)
    private Integer reviewCount = 0;


    @Column(nullable = false)
    private Boolean mastered = false; // user tự đánh dấu

    private LocalDateTime nextReviewAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
