package com.example.learningApp.entity;

import com.example.learningApp.enums.LessonLevel;
import com.example.learningApp.enums.LessonProcess;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    private String title;
    private String description;
    private Integer lessonOrder;

    private LocalDateTime createdAt ;
}

