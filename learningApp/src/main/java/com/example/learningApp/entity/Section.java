package com.example.learningApp.entity;

import com.example.learningApp.enums.LessonLevel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /** N5 – Sơ cấp */
    private String title;

    @Enumerated(EnumType.STRING)
    private LessonLevel lessonLevel;

    @OneToMany(
            mappedBy = "section",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("lessonOrder ASC")
    private List<Lesson> lessons;


    private LocalDateTime createdAt = LocalDateTime.now();
}
