package com.example.learningApp.service.course.section;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.request.course.section.CreateSectionRequest;
import com.example.learningApp.dto.request.course.section.UpdateSectionRequest;
import com.example.learningApp.dto.response.course.section.SectionResponse;
import com.example.learningApp.entity.Course;
import com.example.learningApp.entity.Section;
import com.example.learningApp.mapper.SectionMapper;
import com.example.learningApp.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SectionService {

    private final SectionRepository sectionRepository;
    private final EntityFinder entityFinder;
    private final SectionMapper sectionMapper;

    /* ===================== CREATE ===================== */
    /* ===================== UPDATE ===================== */

    @Transactional
    public String updateSection(String sectionId, UpdateSectionRequest request) {

        Section section = entityFinder.sectionId(sectionId);

        if (request.getTitle() != null) {
            section.setTitle(request.getTitle());
        }

        if (request.getLessonLevel() != null) {
            section.setLessonLevel(request.getLessonLevel());
        }

        return "Update section successfully";
    }
    @Transactional
    public String createSection(CreateSectionRequest request) {

        Course course = entityFinder.courseById(request.getCourseId());

        Section section = sectionMapper.toSection(request);
        section.setCourse(course);
        sectionRepository.save(section);

        return "Create section successfully";
    }

    /* ===================== GET BY COURSE ===================== */

    @Transactional(readOnly = true)
    public List<SectionResponse> getSectionsByCourse(String courseId) {

        return sectionRepository.findByCourseIdOrderByCreatedAtAsc(courseId)
                .stream()
                .map(sectionMapper::toSectionResponse)
                .toList();
    }

    /* ===================== GET DETAIL ===================== */

    @Transactional(readOnly = true)
    public SectionResponse getSectionDetail(String sectionId) {

        Section section = entityFinder.sectionId(sectionId);
        return sectionMapper.toSectionResponse(section);
    }

    /* ===================== DELETE ===================== */

    @Transactional
    public void deleteSection(String sectionId) {

        Section section = entityFinder.sectionId(sectionId);
        sectionRepository.delete(section);
    }
}
