package com.example.learningApp.service.progress.core;

import com.example.learningApp.entity.UserSkillProgress;
import com.example.learningApp.enums.SkillCategory;
import com.example.learningApp.repository.UserSkillProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SkillProgressUpdater {

    private final UserSkillProgressRepository repository;
    private final SkillProgressCalculator calculator;

    public void update(String userId,
                       SkillCategory category,
                       int total,
                       int correct) {

        if (total == 0) return;

        UserSkillProgress progress = repository
                .findByUserIdAndSkillCategory(userId, category)
                .orElse(
                        UserSkillProgress.builder()
                                .userId(userId)
                                .skillCategory(category)
                                .totalQuestions(0)
                                .correctQuestions(0)
                                .accuracy(0)
                                .build()
                );

        progress.setTotalQuestions(progress.getTotalQuestions() + total);
        progress.setCorrectQuestions(progress.getCorrectQuestions() + correct);

        double accuracy = calculator.calculateAccuracy(
                progress.getCorrectQuestions(),
                progress.getTotalQuestions()
        );

        progress.setAccuracy(accuracy);

        repository.save(progress);
    }
}
