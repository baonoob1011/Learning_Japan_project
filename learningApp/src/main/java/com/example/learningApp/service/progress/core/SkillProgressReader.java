package com.example.learningApp.service.progress.core;

import com.example.learningApp.entity.UserSkillProgress;
import com.example.learningApp.enums.SkillCategory;
import com.example.learningApp.repository.UserSkillProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SkillProgressReader {

    private final UserSkillProgressRepository repository;

    public double getAccuracy(String userId, SkillCategory category) {
        return repository.findByUserIdAndSkillCategory(userId, category)
                .map(UserSkillProgress::getAccuracy)
                .orElse(0.0);
    }
}
