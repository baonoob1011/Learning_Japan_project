package com.example.learningApp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "exams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(length = 50, nullable = false)
    private String code;        // VD: JLPT-N1 07 2024

    @Column(length = 10, nullable = false)
    private String level;       // N1/N2/N3/N4/N5

    @Column(nullable = false)
    private Integer duration;   // phút

    @Column(name = "num_sections")
    private Integer numSections;

    @Column(name = "num_questions")
    private Integer numQuestions;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
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
