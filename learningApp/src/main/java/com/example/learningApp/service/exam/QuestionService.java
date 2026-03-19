package com.example.learningApp.service.exam;

import com.example.learningApp.dto.request.exam.CreateQuestionRequest;
import com.example.learningApp.dto.request.exam.question.UpdateQuestionRequest;
import com.example.learningApp.dto.response.exam.QuestionResponse;
import com.example.learningApp.entity.ExamSection;
import com.example.learningApp.entity.Question;
import com.example.learningApp.mapper.QuestionMapper;
import com.example.learningApp.repository.ExamSectionRepository;
import com.example.learningApp.repository.QuestionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuestionService {

    QuestionRepository questionRepository;
    ExamSectionRepository sectionRepository;
    QuestionMapper questionMapper;
    ExamCacheService examCacheService;

    public List<QuestionResponse> getQuestionsBySection(String sectionId) {
        return questionRepository.findBySectionId(sectionId).stream()
                .map(questionMapper::toQuestionResponse)
                .toList();
    }

    public List<QuestionResponse> getAll() {
        return questionRepository.findAll().stream()
                .map(questionMapper::toQuestionResponse)
                .toList();
    }

    @Transactional
    public QuestionResponse updateQuestion(String questionId, UpdateQuestionRequest request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        Set<String> affectedExamIds = collectAffectedExamIds(question);

        if (request.getSectionId() != null) {
            ExamSection section = sectionRepository.findById(request.getSectionId())
                    .orElseThrow(() -> new IllegalArgumentException("Section not found"));
            collectSectionExamIds(section, affectedExamIds);
            question.setSection(section);
        }

        questionMapper.updateQuestion(question, request);
        Question savedQuestion = questionRepository.save(question);

        affectedExamIds.addAll(collectAffectedExamIds(savedQuestion));
        refreshExamCaches(affectedExamIds);

        return questionMapper.toQuestionResponse(savedQuestion);
    }

    @Transactional
    public QuestionResponse createQuestion(CreateQuestionRequest request) {
        sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new IllegalArgumentException("Section not found"));

        Question savedQuestion = questionRepository.save(questionMapper.toQuestion(request));
        refreshExamCaches(collectAffectedExamIds(savedQuestion));
        return questionMapper.toQuestionResponse(savedQuestion);
    }

    public List<QuestionResponse> getQuestionsByExamId(String examId) {
        return questionRepository.findAllByExamId(examId).stream()
                .map(questionMapper::toQuestionResponse)
                .toList();
    }

    @Transactional
    public void deleteQuestion(String questionId) {
        Question question = questionRepository.findById(questionId).orElse(null);
        if (question == null) {
            throw new IllegalArgumentException("Question not found");
        }

        Set<String> affectedExamIds = collectAffectedExamIds(question);
        questionRepository.deleteById(questionId);
        refreshExamCaches(affectedExamIds);
    }

    private Set<String> collectAffectedExamIds(Question question) {
        Set<String> examIds = new HashSet<>();
        if (question == null) {
            return examIds;
        }

        if (question.getExams() != null) {
            question.getExams().stream()
                    .map(exam -> exam.getId())
                    .filter(Objects::nonNull)
                    .forEach(examIds::add);
        }

        collectSectionExamIds(question.getSection(), examIds);
        return examIds;
    }

    private void collectSectionExamIds(ExamSection section, Set<String> examIds) {
        if (section == null || section.getExams() == null) {
            return;
        }
        section.getExams().stream()
                .map(exam -> exam.getId())
                .filter(Objects::nonNull)
                .forEach(examIds::add);
    }

    private void refreshExamCaches(Set<String> examIds) {
        for (String examId : examIds) {
            List<Question> questions = questionRepository.findAllByExamId(examId);
            Set<ExamSection> sections = questions.stream()
                    .map(Question::getSection)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            if (sections.isEmpty()) {
                examCacheService.evictExamCache(examId);
            } else {
                examCacheService.buildAndCache(examId, sections);
            }
        }
    }
}
