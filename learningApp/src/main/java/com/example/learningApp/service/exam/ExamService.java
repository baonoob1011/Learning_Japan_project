package com.example.learningApp.service.exam;

import com.example.learningApp.dto.cache.QuestionCache;
import com.example.learningApp.dto.cache.SectionCache;
import com.example.learningApp.dto.request.exam.CreateExamRequest;
import com.example.learningApp.dto.response.exam.ExamResponse;
import com.example.learningApp.dto.response.exam.SectionWithQuestionsResponse;
import com.example.learningApp.entity.Exam;
import com.example.learningApp.mapper.ExamMapper;
import com.example.learningApp.repository.ExamRepository;
import com.example.learningApp.repository.QuestionRepository;
import com.example.learningApp.repository.ExamParticipantRepository;
import com.example.learningApp.repository.UserExamResultRepository;
import com.example.learningApp.repository.UserAnswerRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExamService {
        RedisTemplate<String, Object> redisTemplate;

        ExamRepository examRepository;
        QuestionRepository questionRepository;
        ExamParticipantRepository examParticipantRepository;
        UserExamResultRepository userExamResultRepository;
        UserAnswerRepository userAnswerRepository;
        ExamMapper examMapper;

        // Method để fetch section + question từ Redis, nếu không có thì tìm từ DB
        public List<SectionWithQuestionsResponse> getSectionAndQuestionByExam(String examId) {
                String examQuestionKey = "exam:" + examId + ":questions";
                String examSectionKey = "exam:" + examId + ":sections";

                Map<String, QuestionCache> questionCacheMap = (Map<String, QuestionCache>) redisTemplate.opsForValue()
                                .get(examQuestionKey);
                Map<String, SectionCache> sectionCacheMap = (Map<String, SectionCache>) redisTemplate.opsForValue()
                                .get(examSectionKey);

                // Nếu cache không có, tìm kiếm từ Database
                if (questionCacheMap == null || sectionCacheMap == null) {
                        log.warn("Exam questions/sections not found in cache for examId: {}. Fetching from database...",
                                        examId);
                        return getSectionAndQuestionFromDatabase(examId);
                }

                // ...existing code...
                Map<String, List<SectionWithQuestionsResponse.QuestionItem>> sectionQuestionsMap = questionCacheMap
                                .values().stream()
                                .map(q -> {
                                        SectionCache sectionCache = sectionCacheMap.get(q.getSectionId());
                                        return SectionWithQuestionsResponse.QuestionItem.builder()
                                                        .id(q.getId())
                                                        .sectionOrder(sectionCache != null
                                                                        ? sectionCache.getSectionOrder()
                                                                        : 0)
                                                        .questionType(q.getQuestionType())
                                                        .questionText(q.getQuestionText())
                                                        .options(q.getOptions())
                                                        .answer(q.getAnswer())
                                                        .imageUrl(q.getImageUrl())
                                                        .audioUrl(q.getAudioUrl())
                                                        .questionOrder(q.getQuestionOrder())
                                                        .build();
                                })
                                .collect(Collectors.groupingBy(
                                                q -> questionCacheMap.get(q.getId()).getSectionId(),
                                                LinkedHashMap::new,
                                                Collectors.toList()));

                List<SectionWithQuestionsResponse> result = sectionCacheMap.values().stream()
                                .map(section -> SectionWithQuestionsResponse.builder()
                                                .id(section.getId())
                                                .examId(examId)
                                                .title(section.getTitle())
                                                .sectionOrder(section.getSectionOrder())
                                                .sectionDuration(section.getSectionDuration())
                                                .questions(
                                                                sectionQuestionsMap
                                                                                .getOrDefault(section.getId(),
                                                                                                List.of())
                                                                                .stream()
                                                                                .sorted(Comparator.comparingInt(
                                                                                                SectionWithQuestionsResponse.QuestionItem::getQuestionOrder))
                                                                                .toList())
                                                .build())
                                .sorted(Comparator.comparingInt(SectionWithQuestionsResponse::getSectionOrder))
                                .toList();

                return result;
        }

        /**
         * Fallback method: Tìm kiếm section và question từ Database
         */
        private List<SectionWithQuestionsResponse> getSectionAndQuestionFromDatabase(String examId) {
                Exam exam = examRepository.findById(examId)
                                .orElseThrow(() -> new RuntimeException("Exam not found with id: " + examId));

                return exam.getSections().stream()
                                .map(section -> SectionWithQuestionsResponse.builder()
                                                .id(section.getId())
                                                .examId(examId)
                                                .title(section.getTitle())
                                                .sectionOrder(section.getSectionOrder())
                                                .sectionDuration(section.getSectionDuration())
                                                .questions(
                                                                section.getQuestions().stream()
                                                                                .map(question -> SectionWithQuestionsResponse.QuestionItem
                                                                                                .builder()
                                                                                                .id(question.getId())
                                                                                                .sectionOrder(section
                                                                                                                .getSectionOrder())
                                                                                                .questionType(question
                                                                                                                .getQuestionType())
                                                                                                .questionText(question
                                                                                                                .getQuestionText())
                                                                                                .options(question
                                                                                                                .getOptions())
                                                                                                .answer(question.getAnswer())
                                                                                                .imageUrl(question
                                                                                                                .getImageUrl())
                                                                                                .audioUrl(question
                                                                                                                .getAudioUrl())
                                                                                                .questionOrder(question
                                                                                                                .getQuestionOrder())
                                                                                                .build())
                                                                                .sorted(Comparator.comparingInt(
                                                                                                SectionWithQuestionsResponse.QuestionItem::getQuestionOrder))
                                                                                .toList())
                                                .build())
                                .sorted(Comparator.comparingInt(SectionWithQuestionsResponse::getSectionOrder))
                                .toList();
        }

        public List<ExamResponse> searchExams(String keyword) {
                List<Exam> exams = examRepository.searchByKeyword(keyword);
                return exams.stream()
                                .map(exam -> {
                                        ExamResponse response = examMapper.toExamResponse(exam);
                                        if (response.getNumQuestions() == null || response.getNumQuestions() == 0) {
                                                long count = questionRepository.countAllByExamId(exam.getId());
                                                response.setNumQuestions((int) count);
                                        }
                                        return response;
                                })
                                .collect(Collectors.toList());
        }

        public List<ExamResponse> getAllExams() {
                // Map từ List<Exam> sang List<ExamResponse>
                return examRepository.findAll().stream()
                                .map(exam -> {
                                        ExamResponse response = examMapper.toExamResponse(exam);
                                        // Nếu số câu hỏi bằng 0, tính toán lại từ database
                                        if (response.getNumQuestions() == null || response.getNumQuestions() == 0) {
                                                long count = questionRepository.countAllByExamId(exam.getId());
                                                response.setNumQuestions((int) count);
                                        }
                                        return response;
                                })
                                .toList();
        }

        public ExamResponse getExamById(String id) {
                Exam exam = examRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Exam not found with id: " + id));
                ExamResponse response = examMapper.toExamResponse(exam);
                if (response.getNumQuestions() == null || response.getNumQuestions() == 0) {
                        long count = questionRepository.countAllByExamId(exam.getId());
                        response.setNumQuestions((int) count);
                }
                return response;
        }

        public ExamResponse createExam(CreateExamRequest createExamRequest) {
                try {
                        // Kiểm tra code đã tồn tại chưa
                        boolean exists = examRepository.existsByCode(createExamRequest.getCode());
                        if (exists) {
                                throw new RuntimeException("Exam code already exists: " + createExamRequest.getCode());
                        }

                        // Lưu entity
                        Exam savedExam = examRepository.save(examMapper.toExam(createExamRequest));

                        // Map entity → response
                        return examMapper.toExamResponse(savedExam);
                } catch (RuntimeException e) {
                        log.error("Validation error while creating exam: {}", e.getMessage());
                        throw e; // có thể ném tiếp để controller xử lý
                } catch (Exception e) {
                        log.error("Unexpected error while creating exam", e);
                        throw new RuntimeException("Failed to create exam. Please try again later.");
                }
        }

        @Transactional
        public void deleteExam(String id) {
                Exam exam = examRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Exam not found with id: " + id));

                log.info("Starting deletion of exam: {} (ID: {})", exam.getCode(), id);

                // 1. Delete associated results and participants
                userExamResultRepository.deleteByExam_Id(id);
                examParticipantRepository.deleteByExam_Id(id);
                userAnswerRepository.deleteByExamId(id);

                // 2. Clear associations (Many-to-Many)
                exam.getSections().clear();
                exam.getQuestions().clear();
                examRepository.save(exam);

                // 3. Delete the exam itself
                examRepository.delete(exam);
                log.info("Successfully deleted exam and related data for id: {}", id);

                // 4. Clean up Redis cache
                redisTemplate.delete("exam:" + id + ":questions");
                redisTemplate.delete("exam:" + id + ":sections");
        }
}
