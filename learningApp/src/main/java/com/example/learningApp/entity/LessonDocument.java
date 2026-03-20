package com.example.learningApp.entity;

import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String documentUrl;

    private Integer documentOrder;
}

