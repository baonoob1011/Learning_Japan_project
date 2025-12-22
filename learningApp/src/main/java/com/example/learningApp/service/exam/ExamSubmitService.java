package com.example.learningApp.service.exam;

import com.example.learningApp.dto.cache.ExamAnswerCache;
import com.example.learningApp.dto.cache.QuestionCache;
import com.example.learningApp.dto.cache.SectionCache;
import com.example.learningApp.dto.request.exam.SubmitExamRequest;
import com.example.learningApp.dto.response.exam.SubmitExamResponse;
import com.example.learningApp.entity.AssessmentItem;
import com.example.learningApp.entity.ExamAnswer;
import com.example.learningApp.entity.ExamParticipant;
import com.example.learningApp.entity.Question;
import com.example.learningApp.repository.ExamAnswerRepository;
import com.example.learningApp.repository.ExamParticipantRepository;
import com.example.learningApp.repository.QuestionRepository;
import com.example.learningApp.service.ai.ResultAIReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamSubmitService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ExamParticipantRepository participantRepo;
    private final QuestionRepository questionRepo;
    private final ExamAnswerRepository examAnswerRepository;

    /**
     * Submit bài thi: tính điểm dựa trên AssessmentItem qua Section
     */
    @Transactional
    public SubmitExamResponse submitExam(String participantId) {

        ExamParticipant participant = participantRepo.findById(participantId)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));

        if (Boolean.TRUE.equals(participant.getCompleted())) {
            throw new IllegalStateException("Exam already submitted");
        }

        LocalDateTime expiredAt = participant.getStartedAt()
                .plusMinutes(participant.getExam().getDuration());

        if (LocalDateTime.now().isAfter(expiredAt)) {
            throw new IllegalStateException("Exam time expired");
        }

        // ===== Lấy question + section cache =====
        String examQuestionKey = "exam:" + participant.getExam().getId() + ":questions";
        String examSectionKey = "exam:" + participant.getExam().getId() + ":sections";

        Map<String, QuestionCache> questionCacheMap =
                (Map<String, QuestionCache>) redisTemplate.opsForValue().get(examQuestionKey);
        Map<String, SectionCache> sectionCacheMap =
                (Map<String, SectionCache>) redisTemplate.opsForValue().get(examSectionKey);

        // ===== Lấy answer cache theo participantId =====
        String answerKey = "exam:participant:" + participant.getId() + ":answers";
        Map<String, ExamAnswerCache> savedAnswerCacheMap =
                (Map<String, ExamAnswerCache>) redisTemplate.opsForValue().get(answerKey);

        if (savedAnswerCacheMap == null || savedAnswerCacheMap.isEmpty()) {
            throw new IllegalStateException("No answers found for participant");
        }

        float totalScore = 0;

        for (ExamAnswerCache cache : savedAnswerCacheMap.values()) {

            QuestionCache questionCache = questionCacheMap.get(cache.getQuestionId());
            if (questionCache == null) {
                throw new IllegalStateException("Question cache not found for id: " + cache.getQuestionId());
            }

            SectionCache section = sectionCacheMap.get(questionCache.getSectionId());
            if (section == null) {
                throw new IllegalStateException("Section cache not found for id: " + questionCache.getSectionId());
            }

            float point = section.getPointMap().getOrDefault(questionCache.getQuestionType(), 1f);

            boolean correct = questionCache.getCorrectAnswer() != null &&
                    questionCache.getCorrectAnswer().trim().equalsIgnoreCase(
                            cache.getAnswer() != null ? cache.getAnswer().trim() : ""
                    );

            // Tạo ExamAnswer mới
            ExamAnswer answer = new ExamAnswer();
            answer.setParticipant(participant);
            answer.setQuestionId(cache.getQuestionId());

            // Snapshot từ Question entity
            Question qEntity = questionRepo.findById(cache.getQuestionId())
                    .orElseThrow(() -> new IllegalArgumentException("Question not found"));

            answer.setQuestionText(qEntity.getQuestionText());
            answer.setQuestionType(qEntity.getQuestionType());
            answer.setOptions(qEntity.getOptions());
            answer.setOrderNum(qEntity.getOrderNum());
            answer.setCorrectAnswer(qEntity.getAnswer());

            // User answer
            answer.setAnswer(cache.getAnswer());
            answer.setAnsweredAt(cache.getAnsweredAt());
            answer.setIsCorrect(correct);
            answer.setScore(correct ? point : 0f);

            totalScore += answer.getScore();
            examAnswerRepository.save(answer);
        }

        // Cập nhật participant
        participant.setScore(totalScore);
        participant.setCompleted(true);
        participant.setFinishedAt(LocalDateTime.now());
        participantRepo.save(participant);

        // Xóa cache
        redisTemplate.delete(answerKey);

        return SubmitExamResponse.builder()
                .participantId(participant.getId())
                .examId(participant.getExam().getId())
                .examCode(participant.getExam().getCode())
                .score(totalScore)
                .completed(true)
                .startedAt(participant.getStartedAt())
                .finishedAt(participant.getFinishedAt())
                .build();
    }



    /**
         * Auto submit khi hết giờ
         */
    @Transactional
    @Scheduled(fixedRate = 60000) // chạy mỗi 1 phút
    public void autoSubmitExpiredExams() {

        LocalDateTime now = LocalDateTime.now();

        // Lấy tất cả participant chưa completed
        List<ExamParticipant> participants = participantRepo.findByCompletedFalse();

        for (ExamParticipant participant : participants) {

            LocalDateTime expiredAt = participant.getStartedAt().plusMinutes(participant.getExam().getDuration());

            if (now.isAfter(expiredAt)) {

                String examQuestionKey = "exam:" + participant.getExam().getId() + ":questions";
                String examSectionKey = "exam:" + participant.getExam().getId() + ":sections";
                String answerKey = "exam:participant:" + participant.getId() + ":answers";

                Map<String, QuestionCache> questionCacheMap =
                        (Map<String, QuestionCache>) redisTemplate.opsForValue().get(examQuestionKey);
                Map<String, SectionCache> sectionCacheMap =
                        (Map<String, SectionCache>) redisTemplate.opsForValue().get(examSectionKey);

                Map<String, ExamAnswerCache> savedAnswerCacheMap =
                        (Map<String, ExamAnswerCache>) redisTemplate.opsForValue().get(answerKey);

                if (savedAnswerCacheMap == null || savedAnswerCacheMap.isEmpty()) {
                    savedAnswerCacheMap = Map.of(); // tránh null
                }

                float totalScore = 0;

                for (ExamAnswerCache cache : savedAnswerCacheMap.values()) {

                    QuestionCache questionCache = questionCacheMap.get(cache.getQuestionId());
                    if (questionCache == null) continue; // skip nếu cache mất

                    SectionCache section = sectionCacheMap.get(questionCache.getSectionId());
                    if (section == null) continue; // skip nếu cache mất

                    float point = section.getPointMap().getOrDefault(questionCache.getQuestionType(), 1f);

                    boolean correct = questionCache.getCorrectAnswer() != null &&
                            questionCache.getCorrectAnswer().trim().equalsIgnoreCase(
                                    cache.getAnswer() != null ? cache.getAnswer().trim() : ""
                            );

                    // Tạo ExamAnswer mới
                    ExamAnswer answer = new ExamAnswer();
                    answer.setParticipant(participant);
                    answer.setQuestionId(cache.getQuestionId());

                    // Snapshot từ Question entity
                    Question qEntity = questionRepo.findById(cache.getQuestionId())
                            .orElseThrow(() -> new IllegalArgumentException("Question not found"));

                    answer.setQuestionText(qEntity.getQuestionText());
                    answer.setQuestionType(qEntity.getQuestionType());
                    answer.setOptions(qEntity.getOptions());
                    answer.setOrderNum(qEntity.getOrderNum());
                    answer.setCorrectAnswer(qEntity.getAnswer());

                    // User answer
                    answer.setAnswer(cache.getAnswer());
                    answer.setAnsweredAt(cache.getAnsweredAt() != null ? cache.getAnsweredAt() : expiredAt);
                    answer.setIsCorrect(correct);
                    answer.setScore(correct ? point : 0f);

                    totalScore += answer.getScore();
                    examAnswerRepository.save(answer);
                }

                // Cập nhật participant
                participant.setScore(totalScore);
                participant.setCompleted(true);
                participant.setFinishedAt(expiredAt);
                participantRepo.save(participant);

                // Xóa cache participant sau khi submit
                redisTemplate.delete(answerKey);
            }
        }
    }


}

