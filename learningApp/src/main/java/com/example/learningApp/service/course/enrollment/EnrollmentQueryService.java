package com.example.learningApp.service.course.enrollment;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.response.course.CourseResponse;
import com.example.learningApp.mapper.CourseMapper;
import com.example.learningApp.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentQueryService {

    private final EnrollmentRepository enrollmentRepository;
    private final EntityFinder entityFinder;
    private final CourseMapper courseMapper;

    public List<CourseResponse> getMyCourses() {

        var user = entityFinder.userById();

        return enrollmentRepository
                .findCoursesByUserId(user.getId())
                .stream()
                .map(courseMapper::toCourseResponse)
                .toList();
    }
}
