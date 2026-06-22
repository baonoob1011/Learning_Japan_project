package com.example.learningApp.service.course.section.lesson;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.request.course.section.lesson.CreateLessonRequest;
import com.example.learningApp.dto.response.lesson.LessonResponse;
import com.example.learningApp.entity.Lesson;
import com.example.learningApp.entity.Section;
import com.example.learningApp.mapper.LessonMapper;
import com.example.learningApp.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final EntityFinder finder;
    private final LessonMapper lessonMapper;

    @Transactional
    public String createLesson(CreateLessonRequest request) {

        Section section = finder.sectionId(request.getSectionId());
        Lesson lesson = lessonMapper.toLesson(request);
        lesson.setSection(section);
        lesson.setCreatedAt(LocalDateTime.now());
        lessonRepository.save(lesson);

        return "Create lesson successfully";
    }


    /* ===================== GET BY SECTION ===================== */

    @Transactional(readOnly = true)
    public List<LessonResponse> getLessonsBySection(String sectionId) {

        return lessonRepository.findBySectionIdOrderByLessonOrderAsc(sectionId)
                .stream()
                .map(lessonMapper::toLessonResponse)
                .toList();
    }

    /* ===================== GET DETAIL ===================== */

    @Transactional(readOnly = true)
    public LessonResponse getLessonDetail(String lessonId) {

        Lesson lesson = finder.lessonId(lessonId);
        return lessonMapper.toLessonResponse(lesson);
    }
    @Transactional
    public String updateLesson(String lessonId, CreateLessonRequest request) {

        Lesson lesson = finder.lessonId(lessonId);

        // Nếu cho phép đổi section
        if (request.getSectionId() != null) {
            Section section = finder.sectionId(request.getSectionId());
            lesson.setSection(section);
        }

        lesson.setTitle(request.getTitle());
        lesson.setDescription(request.getDescription());
        lesson.setLessonOrder(request.getLessonOrder());

        lessonRepository.save(lesson);

        return "Update lesson successfully";
    }
    /* ===================== DELETE ===================== */

    @Transactional
    public void deleteLesson(String lessonId) {

        Lesson lesson = finder.lessonId(lessonId);
        lessonRepository.delete(lesson);
    }
}

