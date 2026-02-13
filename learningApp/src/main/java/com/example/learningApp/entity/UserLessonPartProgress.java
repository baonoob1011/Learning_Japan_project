package com.example.learningApp.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_lesson_part_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLessonPartProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_part_id")
    private LessonPart lessonPart;

    private Double progressPercent;     // 0 → 100

    private Double lastWatchedSecond;   // để resume

    private Boolean completed;

    private LocalDateTime completedAt;
}
