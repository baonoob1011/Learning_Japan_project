package com.example.learningApp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private ExamSection section;

    private String type;           // MCQ, FILL_BLANK, ESSAY, READING
    @Column(columnDefinition = "TEXT")
    private String questionText;

    @Column(columnDefinition = "jsonb")
    private String options;        // lưu JSON dạng String
    @Column(columnDefinition = "jsonb")
    private String answer;         // đáp án chuẩn
    private String imageUrl;
    private String audioUrl;
    private Integer orderNum;

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
