package com.example.learningApp.service.course.section.lesson;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.request.course.section.lesson.UpdateLessonPartProgressRequest;
import com.example.learningApp.dto.response.lesson.LessonPartProgressResponse;
import com.example.learningApp.entity.LessonPart;
import com.example.learningApp.entity.User;
import com.example.learningApp.entity.UserLessonPartProgress;
import com.example.learningApp.repository.UserLessonPartProgressRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserLessonPartProgressService {

    private final UserLessonPartProgressRepository userLessonPartProgressRepository;
    private final UserLessonProgressService userLessonProgressService;
    private final EntityFinder finder;

    // =====================================================
    // UPDATE PART PROGRESS
    // =====================================================
    @Transactional
    public void updateProgress(UpdateLessonPartProgressRequest request) {

        LessonPart lessonPart = finder.lessonPartId(request.getLessonPartId());
        User user = finder.userById();

        UserLessonPartProgress progress =
                userLessonPartProgressRepository
                        .findByUserIdAndLessonPartId(user.getId(), lessonPart.getId())
                        .orElseGet(() ->
                                UserLessonPartProgress.builder()
                                        .user(user)
                                        .lessonPart(lessonPart)
                                        .progressPercent(0.0)
                                        .lastWatchedSecond(0.0)
                                        .completed(false)
                                        .build()
                        );

        if (request.getProgressPercent() > progress.getProgressPercent()) {
            progress.setProgressPercent(request.getProgressPercent());
            progress.setLastWatchedSecond(request.getLastWatchedSecond());
        }

        if (request.getProgressPercent() >= 90
                && !Boolean.TRUE.equals(progress.getCompleted())) {

            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
        }

        userLessonPartProgressRepository.save(progress);

        // 🔥 Gọi service khác
        userLessonProgressService.updateLessonProgress(user, lessonPart.getLesson());
    }

    // =====================================================
    // GET PART PROGRESS
    // =====================================================
    public LessonPartProgressResponse getProgress(String lessonPartId) {

        User user = finder.userById();

        UserLessonPartProgress progress =
                userLessonPartProgressRepository
                        .findByUserIdAndLessonPartId(user.getId(), lessonPartId)
                        .orElse(null);

        if (progress == null) {
            return LessonPartProgressResponse.builder()
                    .progressPercent(0.0)
                    .lastWatchedSecond(0.0)
                    .completed(false)
                    .build();
        }

        return LessonPartProgressResponse.builder()
                .progressPercent(progress.getProgressPercent())
                .lastWatchedSecond(progress.getLastWatchedSecond())
                .completed(progress.getCompleted())
                .build();
    }
}

