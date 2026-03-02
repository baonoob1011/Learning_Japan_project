package com.example.learningApp.service.course;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.request.course.CreateCourseRequest;
import com.example.learningApp.dto.request.course.UpdateCourseRequest;
import com.example.learningApp.dto.response.course.CourseResponse;
import com.example.learningApp.entity.Course;
import com.example.learningApp.mapper.CourseMapper;
import com.example.learningApp.repository.CourseRepository;
import com.example.learningApp.service.cloud.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final EntityFinder entityFinder;
    private final S3Service s3Service;
    /* ===================== CREATE ===================== */

    @Transactional
    public String createCourse(CreateCourseRequest request) {

        var user = entityFinder.userById();

        Course course = courseMapper.toCourse(request);

        /* ================= PAYMENT ================= */

        if (Boolean.TRUE.equals(request.getIsPaid())) {

            if (request.getPrice() == null || request.getPrice() <= 0) {
                throw new RuntimeException("Price must be greater than 0");
            }

            course.setIsPaid(true);
            course.setPrice(request.getPrice());

        } else {
            course.setIsPaid(false);
            course.setPrice(0L);
        }

        /* ================= IMAGE ================= */

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                String imageUrl =
                        s3Service.uploadFile(request.getImage(), "courses/images");
                course.setImageUrl(imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("Upload image failed");
            }
        }

        course.setCreatedBy(user);
        course.setIsActive(true);
        course.setCreatedAt(LocalDateTime.now());

        courseRepository.save(course);

        return "Create course successfully";
    }
    @Transactional
    public String updateCourse(String courseId, UpdateCourseRequest request) {

        Course course = entityFinder.courseById(courseId);

        if (request.getTitle() != null) {
            course.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }

        if (request.getLevel() != null) {
            course.setLevel(request.getLevel());
        }

        if (request.getLessonProcess() != null) {
            course.setLessonProcess(request.getLessonProcess());
        }

        if (request.getPrice() != null) {

            if (request.getPrice() < 0) {
                throw new RuntimeException("Price must be >= 0");
            }

            course.setPrice(request.getPrice());

            // Auto handle isPaid theo price
            course.setIsPaid(request.getPrice() > 0);
        }

        return "Update course successfully";
    }
    /* ===================== GET ALL ===================== */

    @Transactional(readOnly = true)
    public List<CourseResponse> getActiveCourses() {
        return courseRepository.findByIsActiveTrue()
                .stream()
                .map(courseMapper::toCourseResponse)
                .toList();
    }

    /* ===================== GET DETAIL ===================== */

    @Transactional(readOnly = true)
    public CourseResponse getCourseDetail(String courseId) {
        Course course = entityFinder.courseById(courseId);
        return courseMapper.toCourseResponse(course);
    }

    /* ===================== GET FULL TREE ===================== */
    /**
     * Course → Section → Lesson → SectionDocument
     * Dùng cho màn hình học
     */
    @Transactional(readOnly = true)
    public CourseResponse getCourseTree(String courseId) {

        Course course = courseRepository.findWithTreeById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        return courseMapper.toCourseResponse(course);
    }


    /* ===================== TOGGLE ACTIVE ===================== */

    @Transactional
    public void toggleActive(String courseId) {
        Course course = entityFinder.courseById(courseId);
        course.setIsActive(!course.getIsActive());
    }
}
