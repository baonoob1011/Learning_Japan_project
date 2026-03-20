package com.example.learningApp.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_lesson_progress",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "lesson_id"}
        )
)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLessonProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Lesson lesson;

    @Column(nullable = false)
    private Double progressPercent;

    private Boolean completed;

    private LocalDateTime completedAt;
}

