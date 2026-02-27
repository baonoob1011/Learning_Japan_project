package com.example.learningApp.service.course.section;

import com.example.learningApp.entity.Section;
import com.example.learningApp.entity.User;
import com.example.learningApp.entity.UserSectionProgress;
import com.example.learningApp.repository.UserLessonProgressRepository;
import com.example.learningApp.repository.UserSectionProgressRepository;
import com.example.learningApp.service.course.UserCourseProgressService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserSectionProgressService {

    private final UserLessonProgressRepository userLessonProgressRepository;
    private final UserSectionProgressRepository userSectionProgressRepository;
    private final UserCourseProgressService userCourseProgressService;

    @Transactional
    public void updateSectionProgress(User user, Section section) {

        Double sectionPercent =
                userLessonProgressRepository
                        .calculateSectionPercent(user.getId(), section.getId());

        if (sectionPercent == null) {
            sectionPercent = 0.0;
        }

        sectionPercent = Math.round(sectionPercent * 100.0) / 100.0;

        UserSectionProgress sectionProgress =
                userSectionProgressRepository
                        .findByUserIdAndSectionId(user.getId(), section.getId())
                        .orElseGet(() ->
                                UserSectionProgress.builder()
                                        .user(user)
                                        .section(section)
                                        .progressPercent(0.0)
                                        .completed(false)
                                        .build()
                        );

        // 🔥 THÊM
        sectionProgress.setProgressPercent(sectionPercent);

        boolean shouldBeCompleted = sectionPercent >= 90;

        if (shouldBeCompleted) {
            sectionProgress.setCompleted(true);
            if (sectionProgress.getCompletedAt() == null) {
                sectionProgress.setCompletedAt(LocalDateTime.now());
            }
        } else {
            sectionProgress.setCompleted(false);
            sectionProgress.setCompletedAt(null);
        }

        userSectionProgressRepository.save(sectionProgress);

        userCourseProgressService.updateCourseProgress(user, section.getCourse());
    }

}
