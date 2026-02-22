package com.example.learningApp.service.exam;

import com.example.learningApp.dto.cache.QuestionCache;
import com.example.learningApp.dto.cache.SectionCache;
import com.example.learningApp.dto.request.exam.SubmitExamRequest;
import com.example.learningApp.dto.response.exam.SubmitExamResponse;
import com.example.learningApp.enums.SkillCategory;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExamScoringService {

    @Data
    @Builder
    public static class ScoreResult {
        float totalScore;
        int totalQuestions;
        int correctCount;
        int answeredCount;
        int skippedCount;
        Map<SkillCategory, Integer> totalBySkill;
        Map<SkillCategory, Integer> correctBySkill;
        List<SubmitExamResponse.AnswerDetail> details;
    }

    public ScoreResult calculate(
            Map<String, QuestionCache> questionMap,
            Map<String, SectionCache> sectionMap,
            List<SubmitExamRequest.AnswerDto> answers
    ) {

        Map<String, SubmitExamRequest.AnswerDto> answerMap =
                answers.stream()
                        .collect(Collectors.toMap(
                                SubmitExamRequest.AnswerDto::getQuestionId,
                                a -> a
                        ));

        Map<SkillCategory, Integer> totalBySkill = new EnumMap<>(SkillCategory.class);
        Map<SkillCategory, Integer> correctBySkill = new EnumMap<>(SkillCategory.class);

        for (SkillCategory c : SkillCategory.values()) {
            totalBySkill.put(c, 0);
            correctBySkill.put(c, 0);
        }

        float totalScore = 0f;
        List<SubmitExamResponse.AnswerDetail> details = new ArrayList<>();

        for (QuestionCache q : questionMap.values()) {

            SectionCache section = sectionMap.get(q.getSectionId());
            if (section == null) continue;

            float point = section.getPointMap()
                    .getOrDefault(q.getQuestionType(), 1f);

            SubmitExamRequest.AnswerDto dto = answerMap.get(q.getId());
            String userAnswer = dto != null ? dto.getAnswer() : null;

            boolean correct =
                    q.getCorrectAnswer() != null &&
                            q.getCorrectAnswer().trim().equalsIgnoreCase(
                                    userAnswer != null ? userAnswer.trim() : ""
                            );

            if (correct) totalScore += point;

            SkillCategory category = q.getQuestionType().getCategory();
            totalBySkill.put(category, totalBySkill.get(category) + 1);
            if (correct) {
                correctBySkill.put(category, correctBySkill.get(category) + 1);
            }

            details.add(
                    SubmitExamResponse.AnswerDetail.builder()
                            .questionId(q.getId())
                            .questionText(q.getQuestionText())
                            .questionType(q.getQuestionType().name())
                            .answer(userAnswer)
                            .correctAnswer(q.getCorrectAnswer())
                            .isCorrect(correct)
                            .score(correct ? point : 0f)
                            .sectionTitle(section.getTitle())
                            .sectionOrder(section.getSectionOrder())
                            .questionOrder(q.getQuestionOrder())
                            .explanation(q.getExplanation())
                            .imageUrl(q.getImageUrl())
                            .audioUrl(q.getAudioUrl())
                            .build()
            );
        }

        int totalQuestions = questionMap.size();
        int correctCount = correctBySkill.values().stream().mapToInt(Integer::intValue).sum();
        int answeredCount = (int) details.stream().filter(d -> d.getAnswer() != null).count();
        int skippedCount = totalQuestions - answeredCount;

        return ScoreResult.builder()
                .totalScore(totalScore)
                .totalQuestions(totalQuestions)
                .correctCount(correctCount)
                .answeredCount(answeredCount)
                .skippedCount(skippedCount)
                .totalBySkill(totalBySkill)
                .correctBySkill(correctBySkill)
                .details(details)
                .build();
    }
}