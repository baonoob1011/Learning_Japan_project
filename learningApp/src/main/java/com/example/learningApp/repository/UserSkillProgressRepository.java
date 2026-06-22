package com.example.learningApp.repository;

import com.example.learningApp.entity.UserSkillProgress;
import com.example.learningApp.enums.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSkillProgressRepository
        extends JpaRepository<UserSkillProgress, String> {

    Optional<UserSkillProgress> findByUserIdAndSkillCategory(
            String userId,
            SkillCategory skillCategory
    );
}
