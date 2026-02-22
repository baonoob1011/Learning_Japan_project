package com.example.learningApp.service.progress.core;

import org.springframework.stereotype.Component;

@Component
public class SkillProgressCalculator {

    public double calculateAccuracy(long correct, long total) {
        if (total == 0) return 0;
        return Math.round(((double) correct / total) * 100);
    }
}