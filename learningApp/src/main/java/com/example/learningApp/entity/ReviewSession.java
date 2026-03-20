package com.example.learningApp.entity;

import com.example.learningApp.enums.ReviewSessionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "review_sessions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_review_session_user_date", columnNames = {"user_id", "session_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "session_date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReviewSessionStatus status = ReviewSessionStatus.PENDING;

    @Builder.Default
    private int dueCount = 0;

    @Builder.Default
    private int overdueInjectedCount = 0;

    @Builder.Default
    private int newCount = 0;

    @Builder.Default
    private int totalCount = 0;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewSessionItem> items = new ArrayList<>();

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

