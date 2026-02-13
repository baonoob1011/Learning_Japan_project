package com.example.learningApp.service.lesson;

import com.example.learningApp.entity.Lesson;
import com.example.learningApp.entity.User;
import com.example.learningApp.entity.UserLessonProgress;
import com.example.learningApp.repository.UserLessonPartProgressRepository;
import com.example.learningApp.repository.UserLessonProgressRepository;
import com.example.learningApp.service.section.UserSectionProgressService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserLessonProgressService {

    private final UserLessonPartProgressRepository userLessonPartProgressRepository;
    private final UserLessonProgressRepository userLessonProgressRepository;
    private final UserSectionProgressService userSectionProgressService;

    @Transactional
    public void updateLessonProgress(User user, Lesson lesson) {

        Double lessonPercent =
                userLessonPartProgressRepository
                        .calculateLessonPercent(user.getId(), lesson.getId());

        if (lessonPercent == null) {
            lessonPercent = 0.0;
        }

        UserLessonProgress lessonProgress =
                userLessonProgressRepository
                        .findByUserIdAndLessonId(user.getId(), lesson.getId())
                        .orElseGet(() ->
                                UserLessonProgress.builder()
                                        .user(user)
                                        .lesson(lesson)
                                        .completed(false)
                                        .build()
                        );

        boolean shouldBeCompleted = lessonPercent >= 90;

        if (shouldBeCompleted && !Boolean.TRUE.equals(lessonProgress.getCompleted())) {
            lessonProgress.setCompleted(true);
            lessonProgress.setCompletedAt(LocalDateTime.now());
        }

        if (!shouldBeCompleted && Boolean.TRUE.equals(lessonProgress.getCompleted())) {
            lessonProgress.setCompleted(false);
            lessonProgress.setCompletedAt(null);
        }

        userLessonProgressRepository.save(lessonProgress);

        // Cascade update Section
        userSectionProgressService.updateSectionProgress(user, lesson.getSection());
    }
}
