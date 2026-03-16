package com.example.learningApp.service.exam;

import com.example.learningApp.dto.cache.QuestionCache;
import com.example.learningApp.dto.cache.SectionCache;
import com.example.learningApp.entity.ExamSection;
import com.example.learningApp.enums.AssessmentType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExamCacheService {

        private final RedisTemplate<String, Object> redisTemplate;

        public Map<String, QuestionCache> getQuestions(String examId) {
                return (Map<String, QuestionCache>) redisTemplate.opsForValue().get("exam:" + examId + ":questions");
        }

        public Map<String, SectionCache> getSections(String examId) {
                return (Map<String, SectionCache>) redisTemplate.opsForValue().get("exam:" + examId + ":sections");
        }

        public void buildAndCache(String examId, Iterable<ExamSection> sections) {

                Map<String, QuestionCache> questionMap = new HashMap<>();
                Map<String, SectionCache> sectionMap = new HashMap<>();

                for (ExamSection section : sections) {

                        Map<AssessmentType, Float> pointMap = new HashMap<>();
                        section.getAssessmentItems()
                                        .forEach(item -> pointMap.put(item.getAssessmentType(),
                                                        item.getPointPerQuestion()));

                        SectionCache sc = new SectionCache(
                                        section.getId(),
                                        section.getTitle(),
                                        section.getSectionOrder(),
                                        section.getSectionDuration(),
                                        pointMap);

                        sectionMap.put(section.getId(), sc);

                        section.getQuestions().forEach(q -> {
                                QuestionCache qc = QuestionCache.builder()
                                                .id(q.getId())
                                                .questionType(q.getQuestionType())
                                                .correctAnswer(q.getAnswer())
                                                .sectionId(section.getId())
                                                .questionText(q.getQuestionText())
                                                .options(q.getOptions())
                                                .questionOrder(q.getQuestionOrder())
                                                .answer(q.getAnswer())
                                                .explanation(q.getExplanation())
                                                .imageUrl(q.getImageUrl())
                                                .audioUrl(q.getAudioUrl())
                                                .passageTitle(q.getPassage() != null ? q.getPassage().getTitle() : null)
                                                .passageContent(q.getPassage() != null ? q.getPassage().getContent()
                                                                : null)
                                                .build();
                                questionMap.put(q.getId(), qc);
                        });
                }

                redisTemplate.opsForValue()
                                .set("exam:" + examId + ":questions", questionMap, Duration.ofHours(5));

                redisTemplate.opsForValue()
                                .set("exam:" + examId + ":sections", sectionMap, Duration.ofHours(5));
        }
}