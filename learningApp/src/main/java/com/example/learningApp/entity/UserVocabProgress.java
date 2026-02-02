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
    private LearningStatus status = LearningStatus.NOT_LEARNED;

    private int reviewCount = 0;

    private LocalDateTime lastReviewedAt;
    private LocalDateTime nextReminderAt;

}
