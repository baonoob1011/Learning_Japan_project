package com.example.learningApp.service.exam;

import com.example.learningApp.component.kafka.Producer;
import com.example.learningApp.dto.cache.QuestionCache;
import com.example.learningApp.dto.cache.SectionCache;
import com.example.learningApp.dto.request.exam.SubmitExamRequest;
import com.example.learningApp.dto.request.progress.UpdateUserLearningProgressRequest;
import com.example.learningApp.dto.response.exam.SubmitExamResponse;
import com.example.learningApp.dto.response.exam.UserExamResultResponse;
import com.example.learningApp.entity.ExamParticipant;
import com.example.learningApp.entity.ExamSection;
import com.example.learningApp.entity.UserExamResult;
import com.example.learningApp.enums.AssessmentType;
import com.example.learningApp.repository.ExamParticipantRepository;
import com.example.learningApp.repository.UserExamResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExamSubmitService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ExamParticipantRepository participantRepo;
    private final Producer producer;
    private static final String PROGRESS_TOPIC = "user-learning-progress";
    private static final String EXAM_RESULT_TOPIC = "user-exam-result";

    /**
     * Submit bài thi: tính điểm dựa trên AssessmentItem qua Section
     */
    @Transactional
    public SubmitExamResponse submitExam(SubmitExamRequest request) {
        // Lấy participant
        ExamParticipant participant = participantRepo.findById(request.getParticipantId())
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        if (Boolean.TRUE.equals(participant.getCompleted())) {
            throw new RuntimeException("Exam already submitted");
        }

        // Kiểm tra thời gian
        LocalDateTime expiredAt = participant.getStartedAt()
                .plusMinutes(participant.getExam().getDuration());
        if (LocalDateTime.now().isAfter(expiredAt)) {
            throw new RuntimeException("Exam time expired");
        }

        // Redis key
        String examQuestionKey = "exam:" + participant.getExam().getId() + ":questions";
        String examSectionKey = "exam:" + participant.getExam().getId() + ":sections";

        Map<String, QuestionCache> questionCacheMap =
                (Map<String, QuestionCache>) redisTemplate.opsForValue().get(examQuestionKey);
        Map<String, SectionCache> sectionCacheMap =
                (Map<String, SectionCache>) redisTemplate.opsForValue().get(examSectionKey);

        // Fallback DB nếu Redis null hoặc empty
        if (questionCacheMap == null || questionCacheMap.isEmpty()) {
            questionCacheMap = new HashMap<>();
            sectionCacheMap = new HashMap<>();

            for (ExamSection section : participant.getExam().getSections()) {
                // Map questionType -> pointPerQuestion
                Map<AssessmentType, Float> pointMap = new HashMap<>();
                for (var item : section.getAssessmentItems()) {
                    pointMap.put(item.getAssessmentType(), item.getPointPerQuestion());
                }

                // Lưu SectionCache với sectionOrder, title, duration (hoặc field mới)
                SectionCache sectionCache = new SectionCache(
                        section.getId(),
                        section.getTitle(),
                        section.getSectionOrder(),
                        section.getSectionDuration(), // field mới, ví dụ sectionDuration
                        pointMap
                );
                sectionCacheMap.put(section.getId(), sectionCache);

                // Lưu QuestionCache
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
                }
            }

            // Lưu lại Redis
            redisTemplate.opsForValue().set(examQuestionKey, questionCacheMap, Duration.ofHours(5));
            redisTemplate.opsForValue().set(examSectionKey, sectionCacheMap, Duration.ofHours(5));
        }

        if (questionCacheMap.isEmpty()) {
            throw new RuntimeException("Exam questions not found even in DB");
        }

        // ===== Tính điểm và tạo danh sách câu trả lời =====
        int totalQuestions = questionCacheMap.size();
        float totalScore = 0f;
        List<SubmitExamResponse.AnswerDetail> answerDetails = new ArrayList<>();

        for (QuestionCache questionCache : questionCacheMap.values()) {
            SectionCache section = sectionCacheMap.get(questionCache.getSectionId());
            if (section == null) continue;

            float point = section.getPointMap() != null
                    ? section.getPointMap().getOrDefault(questionCache.getQuestionType(), 1f)
                    : 1f;

            SubmitExamRequest.AnswerDto answerDto = request.getAnswers().stream()
                    .filter(a -> a.getQuestionId().equals(questionCache.getId()))
                    .findFirst()
                    .orElse(null);

            String userAnswer = answerDto != null ? answerDto.getAnswer() : null;

            boolean correct = questionCache.getCorrectAnswer() != null &&
                    questionCache.getCorrectAnswer().trim().equalsIgnoreCase(
                            userAnswer != null ? userAnswer.trim() : ""
                    );

            totalScore += correct ? point : 0f;

            answerDetails.add(SubmitExamResponse.AnswerDetail.builder()
                    .questionId(questionCache.getId())
                    .questionText(questionCache.getQuestionText())
                    .questionType(questionCache.getQuestionType().name())
                    .optionsJson(questionCache.getOptions())
                    .correctAnswer(questionCache.getCorrectAnswer())
                    .answer(userAnswer)
                    .isCorrect(correct)
                    .score(correct ? point : 0f)
                    .questionOrder(questionCache.getQuestionOrder())
                    .sectionOrder(section.getSectionOrder())  // sectionOrder thật
                    .sectionTitle(section.getTitle())         // title thật
                    .sectionDuration(section.getSectionDuration()) // field mới
                    .explanation(questionCache.getExplanation())
                    .imageUrl(questionCache.getImageUrl())
                    .audioUrl(questionCache.getAudioUrl())
                    .build());
        }

        int answeredCount = (int) answerDetails.stream().filter(a -> a.getAnswer() != null).count();
        int correctCount = (int) answerDetails.stream().filter(SubmitExamResponse.AnswerDetail::getIsCorrect).count();
        int skippedCount = totalQuestions - answeredCount;

        // Lưu participant
        participant.setScore(totalScore);
        participant.setCompleted(true);
        participant.setFinishedAt(LocalDateTime.now());
        participantRepo.save(participant);

        UpdateUserLearningProgressRequest progressRequest =
                UpdateUserLearningProgressRequest.builder()
                        .userId(participant.getUser().getId())
                        .level(participant.getExam().getLevel())
                        .totalQuestions(totalQuestions)
                        .correctQuestions(correctCount)
                        .build();

        producer.send(PROGRESS_TOPIC, participant.getUser().getId(), progressRequest);

        UserExamResultResponse dto = UserExamResultResponse.builder()
                .userId(participant.getUser().getId())
                .examId(participant.getExam().getId())
                .totalQuestions(totalQuestions)
                .correctQuestions(correctCount)
                .score(totalScore)
                .submittedAt(LocalDateTime.now())
                .build();

        producer.send(EXAM_RESULT_TOPIC, participant.getUser().getId(), dto);


        return SubmitExamResponse.builder()
                .participantId(participant.getId())
                .examId(participant.getExam().getId())
                .examCode(participant.getExam().getCode())
                .totalScore(totalScore)
                .completed(true)
                .answeredCount(answeredCount)
                .totalQuestions(totalQuestions)
                .correctCount(correctCount)
                .skippedCount(skippedCount)
                .answers(answerDetails)
                .startedAt(participant.getStartedAt())
                .finishedAt(participant.getFinishedAt())
                .build();
    }
}
