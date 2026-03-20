package com.example.learningApp.service.course.enrollment;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnrollmentCheckService {

    private final EnrollmentRepository enrollmentRepository;
    private final EntityFinder entityFinder;

    public boolean isUserEnrolled(String courseId) {

        var user = entityFinder.userById();

        return enrollmentRepository
                .existsByUserIdAndCourseId(
                        user.getId(),
                        courseId);
    }
}
