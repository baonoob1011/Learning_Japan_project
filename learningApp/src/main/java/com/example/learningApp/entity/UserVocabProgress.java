package com.example.learningApp.entity;

import com.example.learningApp.enums.LearningStatus;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocab_id")
    private Vocab vocab;

    @Enumerated(EnumType.STRING)
    private LearningStatus status = LearningStatus.NEW;

    private int reviewCount;       // số lần bấm THUỘC
    private int forgottenCount;    // số lần bấm CHƯA THUỘC

    private LocalDateTime createdAt;
    private LocalDateTime lastReviewedAt;

    private LocalDateTime lastReminderSentAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
