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
public class UserLearningProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * User sở hữu progress này
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Level JLPT: N1–N5
     * Mỗi user có thể có nhiều record, mỗi record ứng với 1 level
     */
    @Column(length = 10, nullable = false)
    private String level;

    /**
     * Tổng số exam đã làm ở level này
     */
    @Column(nullable = false)
    private Integer totalExamsTaken = 0;

    /**
     * Tổng số câu hỏi đã làm
     */
    @Column(nullable = false)
    private Integer totalQuestionsDone = 0;

    /**
     * Tổng số câu đúng
     */
    @Column(nullable = false)
    private Integer correctQuestions = 0;

    /**
     * Điểm trung bình (0–100 hoặc theo thang bạn định nghĩa)
     */
    private Float averageScore;

    /**
     * Thời điểm làm exam gần nhất ở level này
     */
    private LocalDateTime lastExamAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
