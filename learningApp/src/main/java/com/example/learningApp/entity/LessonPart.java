package com.example.learningApp.entity;

import com.example.learningApp.enums.LessonPartType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonPart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Enumerated(EnumType.STRING)
    private LessonPartType lessonPartType;
    @Column(columnDefinition = "TEXT")
    private String videoUrl;
    private String title;
    private String duration;           // PT2H35M54S

    private Integer partOrder;
}
