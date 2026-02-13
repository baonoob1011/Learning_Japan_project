package com.example.learningApp.service.section;

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

        UserSectionProgress sectionProgress =
                userSectionProgressRepository
                        .findByUserIdAndSectionId(user.getId(), section.getId())
                        .orElseGet(() ->
                                UserSectionProgress.builder()
                                        .user(user)
                                        .section(section)
                                        .completed(false)
                                        .build()
                        );

        if (sectionPercent >= 90
                && !Boolean.TRUE.equals(sectionProgress.getCompleted())) {

            sectionProgress.setCompleted(true);
            sectionProgress.setCompletedAt(LocalDateTime.now());
        }

        userSectionProgressRepository.save(sectionProgress);
        userCourseProgressService.updateCourseProgress(user, section.getCourse());

    }
}
