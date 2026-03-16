package com.example.learningApp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "passages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Passage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @EqualsAndHashCode.Include
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer passageOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    @EqualsAndHashCode.Include
    private ExamSection section;

    @OneToMany(mappedBy = "passage", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Question> questions = new LinkedHashSet<>();
}
