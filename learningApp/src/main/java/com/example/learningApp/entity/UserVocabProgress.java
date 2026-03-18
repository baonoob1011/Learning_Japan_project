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

    private int reviewCount; // số lần bấm THUỘC
    private int forgottenCount; // số lần bấm CHƯA THUỘC

    private LocalDateTime createdAt;
    private LocalDateTime lastReviewedAt;

    private LocalDateTime lastReminderSentAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
