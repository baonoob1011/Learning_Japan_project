package com.example.learningApp.service.course;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.response.course.UserCourseProgressResponse;
import com.example.learningApp.entity.Course;
import com.example.learningApp.entity.User;
import com.example.learningApp.entity.UserCourseProgress;
import com.example.learningApp.repository.UserCourseProgressRepository;
import com.example.learningApp.repository.UserSectionProgressRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserCourseProgressService {

    private final UserSectionProgressRepository userSectionProgressRepository;
    private final UserCourseProgressRepository userCourseProgressRepository;
    private final EntityFinder finder;

    @Transactional
    public void updateCourseProgress(User user, Course course) {

        Double coursePercent =
                userSectionProgressRepository
                        .calculateCoursePercent(user.getId(), course.getId());

        if (coursePercent == null) {
            coursePercent = 0.0;
        }

        UserCourseProgress courseProgress =
                userCourseProgressRepository
                        .findByUserIdAndCourseId(user.getId(), course.getId())
                        .orElseGet(() ->
                                UserCourseProgress.builder()
                                        .user(user)
                                        .course(course)
                                        .completed(false)
                                        .build()
                        );

        boolean shouldBeCompleted = coursePercent >= 90;

        if (shouldBeCompleted && !Boolean.TRUE.equals(courseProgress.getCompleted())) {
            courseProgress.setCompleted(true);
            courseProgress.setCompletedAt(LocalDateTime.now());
        }

        if (!shouldBeCompleted && Boolean.TRUE.equals(courseProgress.getCompleted())) {
            courseProgress.setCompleted(false);
            courseProgress.setCompletedAt(null);
        }

        userCourseProgressRepository.save(courseProgress);
    }
    @Transactional
    public UserCourseProgressResponse getCourseProgress(Course course) {

        var user=finder.userById();
        Double percent =
                userSectionProgressRepository
                        .calculateCoursePercent(user.getId(), course.getId());

        if (percent == null) {
            percent = 0.0;
        }

        UserCourseProgress progress =
                userCourseProgressRepository
                        .findByUserIdAndCourseId(user.getId(), course.getId())
                        .orElse(null);

        return UserCourseProgressResponse.builder()
                .courseId(course.getId())
                .percent(percent)
                .completed(progress != null && Boolean.TRUE.equals(progress.getCompleted()))
                .completedAt(progress != null ? progress.getCompletedAt() : null)
                .build();
    }

}
