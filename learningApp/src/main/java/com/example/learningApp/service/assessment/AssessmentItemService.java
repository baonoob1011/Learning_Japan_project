package com.example.learningApp.service.assessment;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.request.exam.assessment.UpdateAssessmentItemRequest;
import com.example.learningApp.dto.response.exam.assessment.AssessmentItemResponse;
import com.example.learningApp.entity.AssessmentItem;
import com.example.learningApp.entity.ExamSection;
import com.example.learningApp.mapper.AssessmentItemMapper;
import com.example.learningApp.repository.AssessmentItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssessmentItemService {

    private final AssessmentItemRepository repository;
    private final EntityFinder finder;
    private final AssessmentItemMapper mapper;

    /* ===================== GET BY SECTION ===================== */

    @Transactional(readOnly = true)
    public List<AssessmentItemResponse> getBySection(String sectionId) {

        ExamSection section = finder.examSectionId(sectionId);

        return repository.findBySection(section)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    /* ===================== GET DETAIL ===================== */

    @Transactional(readOnly = true)
    public AssessmentItemResponse getDetail(String itemId) {

        AssessmentItem item = finder.assessmentItemId(itemId);
        return mapper.toResponse(item);
    }

    /* ===================== UPDATE ===================== */

    @Transactional
    public String update(String itemId, UpdateAssessmentItemRequest request) {

        AssessmentItem item = finder.assessmentItemId(itemId);

        // nếu cho đổi section
        if (request.getSectionId() != null) {
            ExamSection section = finder.examSectionId(request.getSectionId());
            item.setSection(section);
        }

        if (request.getName() != null) {
            item.setName(request.getName());
        }

        if (request.getLevel() != null) {
            item.setLevel(request.getLevel());
        }

        if (request.getQuestionCount() != null) {
            item.setQuestionCount(request.getQuestionCount());
        }

        if (request.getPointPerQuestion() != null) {
            item.setPointPerQuestion(request.getPointPerQuestion());
        }

        if (request.getAssessmentType() != null) {
            item.setAssessmentType(request.getAssessmentType());
        }

        // Auto tính lại totalPoint nếu có đủ dữ liệu
        if (item.getQuestionCount() != null && item.getPointPerQuestion() != null) {
            item.setTotalPoint(
                    item.getQuestionCount() * item.getPointPerQuestion()
            );
        }

        return "Update assessment item successfully";
    }
}