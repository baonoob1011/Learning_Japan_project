package com.example.learningApp.entity;

import com.example.learningApp.enums.SkillCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_skill_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSkillProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String userId;

    @Enumerated(EnumType.STRING)
    private SkillCategory skillCategory;

    private long totalQuestions;
    private long correctQuestions;

    private double accuracy; // % đúng
}
