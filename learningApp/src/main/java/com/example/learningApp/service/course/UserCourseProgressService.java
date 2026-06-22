package com.example.learningApp.service.course;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.response.course.UserCourseProgressResponse;
import com.example.learningApp.entity.Course;
import com.example.learningApp.entity.User;
import com.example.learningApp.entity.UserCourseProgress;
import com.example.learningApp.mapper.CourseMapper;
import com.example.learningApp.mapper.UserCourseProgressMapper;
import com.example.learningApp.repository.UserCourseProgressRepository;
import com.example.learningApp.repository.UserSectionProgressRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserCourseProgressService {

    private final UserSectionProgressRepository userSectionProgressRepository;
    private final UserCourseProgressRepository userCourseProgressRepository;
    private final EntityFinder finder;
    private final CourseMapper courseMapper;

    @Transactional
    public void updateCourseProgress(User user, Course course) {

        Double coursePercent =
                userSectionProgressRepository
                        .calculateCoursePercent(user.getId(), course.getId());

        if (coursePercent == null) {
            coursePercent = 0.0;
        }

        coursePercent = Math.round(coursePercent * 100.0) / 100.0;

        UserCourseProgress courseProgress =
                userCourseProgressRepository
                        .findByUserIdAndCourseId(user.getId(), course.getId())
                        .orElseGet(() ->
                                UserCourseProgress.builder()
                                        .user(user)
                                        .course(course)
                                        .progressPercent(0.0)
                                        .completed(false)
                                        .build()
                        );

        // 🔥 QUAN TRỌNG
        courseProgress.setProgressPercent(coursePercent);

        boolean shouldBeCompleted = coursePercent >= 90;

        if (shouldBeCompleted) {
            courseProgress.setCompleted(true);
            if (courseProgress.getCompletedAt() == null) {
                courseProgress.setCompletedAt(LocalDateTime.now());
            }
        } else {
            courseProgress.setCompleted(false);
            courseProgress.setCompletedAt(null);
        }

        userCourseProgressRepository.save(courseProgress);
    }

    @Transactional
    public UserCourseProgressResponse getCourseProgress(String courseId) {

        User user = finder.userById();

        UserCourseProgress progress =
                userCourseProgressRepository
                        .findByUserIdAndCourseId(user.getId(), courseId)
                        .orElse(null);

        Course course = (progress != null)
                ? progress.getCourse()
                : finder.courseById(courseId);

        double percent = (progress != null && progress.getProgressPercent() != null)
                ? progress.getProgressPercent()
                : 0.0;

        return UserCourseProgressResponse.builder()
                .id(progress != null ? progress.getId() : null)
                .percent(percent)
                .completed(progress != null && Boolean.TRUE.equals(progress.getCompleted()))
                .completedAt(progress != null ? progress.getCompletedAt() : null)
                .course(courseMapper.toCourseResponse(course))
                .build();
    }

    @Transactional
    public List<UserCourseProgressResponse> getAllCourseProgressByUserId() {

        User user = finder.userById(); // nếu đã có method này
        // hoặc nếu muốn lấy user hiện tại:
        // User user = finder.userById();

        List<UserCourseProgress> progressList =
                userCourseProgressRepository.findByUserId(user.getId());

        return progressList.stream()
                .map(progress -> UserCourseProgressResponse.builder()
                        .id(progress.getId())
                        .percent(progress.getProgressPercent() != null
                                ? progress.getProgressPercent()
                                : 0.0)
                        .completed(Boolean.TRUE.equals(progress.getCompleted()))
                        .completedAt(progress.getCompletedAt())
                        .course(courseMapper.toCourseResponse(progress.getCourse()))
                        .build()
                )
                .toList();
    }

}

