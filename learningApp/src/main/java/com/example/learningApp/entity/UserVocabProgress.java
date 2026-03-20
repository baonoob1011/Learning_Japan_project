package com.example.learningApp.entity;

import com.example.learningApp.enums.LearningStatus;
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
public class UserVocabProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocab_id")
    @JsonIgnoreProperties("vocabProgresses")
    private Vocab vocab;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LearningStatus status = LearningStatus.NEW;

    private int reviewCount;
    private int forgottenCount;

    @Builder.Default
    private int intervalDays = 0;

    @Builder.Default
    private double easeFactor = 2.5;

    @Builder.Default
    private int lapseCount = 0;

    @Builder.Default
    private int successCount = 0;

    private LocalDateTime createdAt;
    private LocalDateTime lastReviewedAt;
    private LocalDateTime nextReviewAt;
    private LocalDateTime updatedAt;

    private LocalDateTime lastReminderSentAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (nextReviewAt == null) {
            nextReviewAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

