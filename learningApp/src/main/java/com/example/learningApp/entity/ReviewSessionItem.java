package com.example.learningApp.entity;

import com.example.learningApp.enums.ReviewQueueType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_session_items", indexes = {
        @Index(name = "idx_review_session_order", columnList = "session_id, orderIndex")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewSessionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ReviewSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_progress_id", nullable = false)
    private UserVocabProgress wordProgress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewQueueType queueType;

    @Column(nullable = false)
    private int orderIndex;

    @Builder.Default
    private boolean completed = false;

    private LocalDateTime completedAt;
}

