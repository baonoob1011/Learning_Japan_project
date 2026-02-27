package com.example.learningApp.service.course.section.lesson;

import com.example.learningApp.entity.Lesson;
import com.example.learningApp.entity.User;
import com.example.learningApp.entity.UserLessonProgress;
import com.example.learningApp.repository.UserLessonPartProgressRepository;
import com.example.learningApp.repository.UserLessonProgressRepository;
import com.example.learningApp.service.course.section.UserSectionProgressService;
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

        lessonPercent = Math.round(lessonPercent * 100.0) / 100.0;

        UserLessonProgress lessonProgress =
                userLessonProgressRepository
                        .findByUserIdAndLessonId(user.getId(), lesson.getId())
                        .orElseGet(() ->
                                UserLessonProgress.builder()
                                        .user(user)
                                        .lesson(lesson)
                                        .progressPercent(0.0)
                                        .completed(false)
                                        .build()
                        );

        // 🔥 THÊM DÒNG NÀY
        lessonProgress.setProgressPercent(lessonPercent);

        boolean shouldBeCompleted = lessonPercent >= 90;

        if (shouldBeCompleted) {
            lessonProgress.setCompleted(true);
            if (lessonProgress.getCompletedAt() == null) {
                lessonProgress.setCompletedAt(LocalDateTime.now());
            }
        } else {
            lessonProgress.setCompleted(false);
            lessonProgress.setCompletedAt(null);
        }

        userLessonProgressRepository.save(lessonProgress);

        userSectionProgressService.updateSectionProgress(user, lesson.getSection());
    }

}
