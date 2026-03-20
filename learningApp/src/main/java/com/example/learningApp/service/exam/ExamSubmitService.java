package com.example.learningApp.service.exam;

import com.example.learningApp.common.kafka.Producer;
import com.example.learningApp.dto.cache.QuestionCache;
import com.example.learningApp.dto.cache.SectionCache;
import com.example.learningApp.dto.request.exam.SubmitExamRequest;
import com.example.learningApp.dto.request.progress.UpdateUserLearningProgressRequest;
import com.example.learningApp.dto.response.exam.SubmitExamResponse;
import com.example.learningApp.dto.response.exam.UserExamResultResponse;
import com.example.learningApp.entity.ExamParticipant;
import com.example.learningApp.repository.ExamParticipantRepository;
import com.example.learningApp.service.progress.ProgressTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExamSubmitService {

    private final ExamParticipantRepository participantRepo;
    private final ExamCacheService cacheService;
    private final ExamScoringService scoringService;
    private final ProgressTrackingService progressTrackingService;
    private final Producer producer;

    private static final String PROGRESS_TOPIC = "user-learning-progress";
    private static final String EXAM_RESULT_TOPIC = "user-exam-result";

    @Transactional
    public SubmitExamResponse submitExam(SubmitExamRequest request) {

        ExamParticipant participant = participantRepo.findById(request.getParticipantId())
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        if (Boolean.TRUE.equals(participant.getCompleted())) {
            throw new RuntimeException("Exam already submitted");
        }

        Map<String, QuestionCache> questions =
                cacheService.getQuestions(participant.getExam().getId());

        Map<String, SectionCache> sections =
                cacheService.getSections(participant.getExam().getId());

        if (questions == null || sections == null) {
            cacheService.buildAndCache(
                    participant.getExam().getId(),
                    participant.getExam().getSections()
            );
            questions = cacheService.getQuestions(participant.getExam().getId());
            sections = cacheService.getSections(participant.getExam().getId());
        }

        ExamScoringService.ScoreResult result =
                scoringService.calculate(questions, sections, request.getAnswers());

        participant.setScore(result.getTotalScore());
        participant.setCompleted(true);
        participant.setFinishedAt(LocalDateTime.now());
        participantRepo.save(participant);

        progressTrackingService.updateSkillProgress(
                participant.getUser().getId(),
                result.getTotalBySkill(),
                result.getCorrectBySkill()
        );

        producer.send(PROGRESS_TOPIC,
                participant.getUser().getId(),
                UpdateUserLearningProgressRequest.builder()
                        .userId(participant.getUser().getId())
                        .level(participant.getExam().getLevel())
                        .totalQuestions(result.getTotalQuestions())
                        .correctQuestions(result.getCorrectCount())
                        .build()
        );

        producer.send(EXAM_RESULT_TOPIC,
                participant.getUser().getId(),
                UserExamResultResponse.builder()
                        .userId(participant.getUser().getId())
                        .examId(participant.getExam().getId())
                        .totalQuestions(result.getTotalQuestions())
                        .correctQuestions(result.getCorrectCount())
                        .score(result.getTotalScore())
                        .submittedAt(LocalDateTime.now())
                        .build()
        );

        return SubmitExamResponse.builder()
                .participantId(participant.getId())
                .examId(participant.getExam().getId())
                .examCode(participant.getExam().getCode())
                .totalScore(result.getTotalScore())
                .completed(true)
                .answeredCount(result.getAnsweredCount())
                .correctCount(result.getCorrectCount())
                .skippedCount(result.getSkippedCount())
                .totalQuestions(result.getTotalQuestions())
                .answers(result.getDetails())
                .startedAt(participant.getStartedAt())
                .finishedAt(participant.getFinishedAt())
                .build();
    }
}
