package com.example.learningApp.entity;

import com.example.learningApp.enums.AssessmentType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exam_sections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamSection {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToMany(mappedBy = "sections", fetch = FetchType.LAZY)
    private List<Exam> exams = new ArrayList<>();

    private String title;
    private Integer sectionOrder;
    @Column(nullable = false)
    private Integer sectionDuration; // phút
    private String level;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssessmentItem> assessmentItems; // <-- thêm quan hệ với AssessmentItem

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL)
    private List<Question> questions;


}
