package com.example.learningApp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "exam_sections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ExamSection {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToMany(mappedBy = "sections", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Exam> exams = new LinkedHashSet<>();

    @EqualsAndHashCode.Include
    private String title;
    @EqualsAndHashCode.Include
    private Integer sectionOrder;
    @Column(nullable = false)
    private Integer sectionDuration; // phút
    @EqualsAndHashCode.Include
    private String level;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<AssessmentItem> assessmentItems = new LinkedHashSet<>();

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Passage> passages = new LinkedHashSet<>();

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Question> questions = new LinkedHashSet<>();

}

