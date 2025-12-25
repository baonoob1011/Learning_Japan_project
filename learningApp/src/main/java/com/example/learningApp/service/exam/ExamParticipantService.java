package com.example.learningApp.service.exam;

import com.example.learningApp.dto.cache.QuestionCache;
import com.example.learningApp.dto.cache.SectionCache;
import com.example.learningApp.dto.request.exam.StartExamRequest;
import com.example.learningApp.dto.response.exam.SectionWithQuestionsResponse;
import com.example.learningApp.dto.response.exam.StartExamResponse;
import com.example.learningApp.entity.Exam;
import com.example.learningApp.entity.ExamParticipant;
import com.example.learningApp.entity.ExamSection;
import com.example.learningApp.entity.User;
import com.example.learningApp.enums.AssessmentType;
import com.example.learningApp.repository.ExamParticipantRepository;
import com.example.learningApp.repository.ExamRepository;
import com.example.learningApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamParticipantService {

    private final Logger log = LoggerFactory.getLogger(ExamParticipantService.class);

    private final ExamParticipantRepository participantRepo;
    private final ExamRepository examRepo;
    private final UserRepository userRepo;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public StartExamResponse startExam(StartExamRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        Exam exam = examRepo.findById(request.getExamId())
                .orElseThrow(() -> new RuntimeException("Exam not found"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ExamParticipant participant = ExamParticipant.builder()
                .exam(exam)
                .user(user)
                .startedAt(LocalDateTime.now())
                .completed(false)
                .build();
        participantRepo.save(participant);

        exam.setParticipant(exam.getParticipant() + 1);
        examRepo.save(exam);

        String examQuestionKey = "exam:" + exam.getId() + ":questions";
        String examSectionKey = "exam:" + exam.getId() + ":sections";

        Map<String, QuestionCache> questionCacheMap =
                (Map<String, QuestionCache>) redisTemplate.opsForValue().get(examQuestionKey);
        Map<String, SectionCache> sectionCacheMap =
                (Map<String, SectionCache>) redisTemplate.opsForValue().get(examSectionKey);

        if (questionCacheMap == null || questionCacheMap.isEmpty()) {
            questionCacheMap = new HashMap<>();
            sectionCacheMap = new HashMap<>();

            for (ExamSection section : exam.getSections()) {
                Map<AssessmentType, Float> pointMap = new HashMap<>();
                for (var item : section.getAssessmentItems()) {
                    pointMap.put(item.getAssessmentType(), item.getPointPerQuestion());
                }

                SectionCache sectionCache = new SectionCache(
                        section.getId(),
                        section.getTitle(),
                        section.getSectionOrder(),
                        section.getSectionDuration(), // thêm field duration
                        pointMap
                );
                sectionCacheMap.put(section.getId(), sectionCache);
                log.info("Caching section: {} - order: {}", section.getTitle(), section.getSectionOrder());

                for (var q : section.getQuestions()) {
                    QuestionCache questionCache = new QuestionCache(
                            q.getId(),
                            q.getQuestionType(),
                            q.getAnswer(),
                            section.getId(),
                            q.getQuestionText(),
                            q.getOptions(),
                            q.getQuestionOrder(),
                            q.getAnswer(),
                            q.getExplanation(),
                            q.getImageUrl(),
                            q.getAudioUrl()
                    );
                    questionCacheMap.put(q.getId(), questionCache);
                    log.info("Caching question: {} - sectionOrder: {}", q.getQuestionText(), section.getSectionOrder());
                }
            }

            redisTemplate.opsForValue().set(examQuestionKey, questionCacheMap, Duration.ofHours(5));
            redisTemplate.opsForValue().set(examSectionKey, sectionCacheMap, Duration.ofHours(5));
            log.info("Saved {} sections and {} questions to Redis", sectionCacheMap.size(), questionCacheMap.size());
        } else {
            log.info("Loaded {} sections and {} questions from Redis", sectionCacheMap.size(), questionCacheMap.size());
        }

        Map<String, QuestionCache> finalQuestionCacheMap = questionCacheMap;
        List<SectionWithQuestionsResponse> sections = sectionCacheMap.values().stream()
                .sorted(Comparator.comparing(SectionCache::getSectionOrder))
                .map(section -> {
                    List<SectionWithQuestionsResponse.QuestionItem> questionItems = finalQuestionCacheMap.values().stream()
                            .filter(q -> q.getSectionId().equals(section.getId()))
                            .sorted(Comparator.comparing(QuestionCache::getQuestionOrder))
                            .map(q -> SectionWithQuestionsResponse.QuestionItem.builder()
                                    .id(q.getId())
                                    .sectionOrder(section.getSectionOrder())
                                    .questionType(q.getQuestionType())
                                    .questionText(q.getQuestionText())
                                    .options(q.getOptions())
                                    .answer(q.getAnswer())
                                    .imageUrl(q.getImageUrl())
                                    .audioUrl(q.getAudioUrl())
                                    .questionOrder(q.getQuestionOrder())
                                    .build())
                            .toList();

                    return SectionWithQuestionsResponse.builder()
                            .id(section.getId())
                            .examId(exam.getId())
                            .title(section.getTitle())
                            .sectionOrder(section.getSectionOrder())
                            .questions(questionItems)
                            .build();
                })
                .toList();

        log.info("Returning {} sections for exam {}", sections.size(), exam.getId());

        return StartExamResponse.builder()
                .participantId(participant.getId())
                .examId(exam.getId())
                .examCode(exam.getCode())
                .duration(exam.getDuration())
                .userId(user.getId())
                .completed(false)
                .startedAt(participant.getStartedAt())
                .build();
    }

    public List<SectionWithQuestionsResponse> getSectionAndQuestionByExam(String examId) {
        String examQuestionKey = "exam:" + examId + ":questions";
        String examSectionKey = "exam:" + examId + ":sections";

        Map<String, QuestionCache> questionCacheMap =
                (Map<String, QuestionCache>) redisTemplate.opsForValue().get(examQuestionKey);
        Map<String, SectionCache> sectionCacheMap =
                (Map<String, SectionCache>) redisTemplate.opsForValue().get(examSectionKey);

        if (questionCacheMap == null || sectionCacheMap == null) {
            throw new RuntimeException("Exam questions/sections not found in cache");
        }

        return sectionCacheMap.values().stream()
                .sorted(Comparator.comparing(SectionCache::getSectionOrder))
                .map(section -> {
                    List<SectionWithQuestionsResponse.QuestionItem> questionItems = questionCacheMap.values().stream()
                            .filter(q -> q.getSectionId().equals(section.getId()))
                            .sorted(Comparator.comparing(QuestionCache::getQuestionOrder))
                            .map(q -> SectionWithQuestionsResponse.QuestionItem.builder()
                                    .id(q.getId())
                                    .sectionOrder(section.getSectionOrder())
                                    .questionType(q.getQuestionType())
                                    .questionText(q.getQuestionText())
                                    .options(q.getOptions())
                                    .answer(q.getAnswer())
                                    .imageUrl(q.getImageUrl())
                                    .audioUrl(q.getAudioUrl())
                                    .questionOrder(q.getQuestionOrder())
                                    .build())
                            .toList();

                    return SectionWithQuestionsResponse.builder()
                            .id(section.getId())
                            .examId(examId)
                            .title(section.getTitle())
                            .sectionOrder(section.getSectionOrder())
                            .questions(questionItems)
                            .build();
                })
                .toList();
    }
}
