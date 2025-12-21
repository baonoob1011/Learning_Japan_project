package com.example.learningApp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "exam_answers")
@Getter
@Setter
public class ExamAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // ======================
    // 🔗 RELATION
    // ======================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private ExamParticipant participant;

    // snapshot id (không dùng để join logic)
    @Column(name = "question_id", nullable = false)
    private String questionId;

    // ======================
    // 🔗 RELATION TO QUESTION (read-only)
    // ======================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Question question;

    // ======================
    // 📌 SNAPSHOT QUESTION
    // ======================
    @Column(columnDefinition = "TEXT")
    private String questionText;

    @Column(length = 50)
    private String questionType;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String options;

    @Column(columnDefinition = "TEXT")
    private String correctAnswer;

    // ======================
    // ✍ USER ANSWER
    // ======================
    @Column(columnDefinition = "TEXT")
    private String answer;

    private Boolean isCorrect;
    private Float score;
    private Integer orderNum;

    private LocalDateTime answeredAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
