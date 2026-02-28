package com.example.learningApp.entity;

import com.example.learningApp.enums.FeedbackType;
import com.example.learningApp.enums.FeedbackStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Enumerated(EnumType.STRING)
    private FeedbackType type;
    // BUG, FEATURE_REQUEST, UX_IMPROVEMENT, CONTENT_REQUEST

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String adminReply;

    private String attachmentUrl; // nếu có ảnh/video minh họa

    @Enumerated(EnumType.STRING)
    private FeedbackStatus status;
    // PENDING, REVIEWING, RESOLVED, REJECTED

    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}