package com.example.learningApp.entity;

import com.example.learningApp.enums.AssessmentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AssessmentType questionType;

    @Column(columnDefinition = "TEXT")
    private String questionText;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String options;

    @ManyToMany(mappedBy = "questions")
    @Builder.Default
    private List<Exam> exams = new ArrayList<>();


    @Column(columnDefinition = "TEXT")
    private String answer;
    // 🔹 Thêm giải thích đáp án
    @Column(columnDefinition = "TEXT")
    private String explanation;
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
