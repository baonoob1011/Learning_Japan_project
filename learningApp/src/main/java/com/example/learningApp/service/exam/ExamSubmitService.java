package com.example.learningApp.service.exam;

import com.example.learningApp.dto.request.exam.SubmitExamRequest;
import com.example.learningApp.dto.response.exam.SubmitExamResponse;
import com.example.learningApp.entity.ExamAnswer;
import com.example.learningApp.entity.ExamParticipant;
import com.example.learningApp.entity.Question;
import com.example.learningApp.repository.ExamAnswerRepository;
import com.example.learningApp.repository.ExamParticipantRepository;
import com.example.learningApp.repository.QuestionRepository;
import com.example.learningApp.service.ai.ResultAIReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamSubmitService {

    private final ExamParticipantRepository participantRepo;
    private final QuestionRepository questionRepo;
    private final ExamAnswerRepository examAnswerRepository;

    /**
     * Submit bài thi: lấy trực tiếp answer đã lưu, cập nhật isCorrect và score
     */
    @Transactional
    public SubmitExamResponse submitExam(SubmitExamRequest request) {

        ExamParticipant participant = participantRepo.findById(request.getParticipantId())
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));

        if (Boolean.TRUE.equals(participant.getCompleted())) {
            throw new IllegalStateException("Exam already submitted");
        }

        LocalDateTime expiredAt = participant.getStartedAt()
                .plusMinutes(participant.getExam().getDuration());

        if (LocalDateTime.now().isAfter(expiredAt)) {
            throw new IllegalStateException("Exam time expired");
        }

        // 🔹 1️⃣ Lấy toàn bộ answer đã lưu
        List<ExamAnswer> savedAnswers = examAnswerRepository.findByParticipant_Id(participant.getId());

        float totalScore = 0;

        for (ExamAnswer answer : savedAnswers) {
            Question question = questionRepo.findById(answer.getQuestionId()).orElse(null);
            if (question == null) continue;


            boolean correct = question.getAnswer() != null &&
                    question.getAnswer().trim().equalsIgnoreCase(answer.getAnswer() != null ? answer.getAnswer().trim() : "");

            answer.setIsCorrect(correct);
            answer.setScore(correct ? 1f : 0f);

            totalScore += answer.getScore() != null ? answer.getScore() : 0;

            examAnswerRepository.save(answer);
        }

        // 🔹 2️⃣ Hoàn tất bài thi
        participant.setScore(totalScore);
        participant.setCompleted(true);
        participant.setFinishedAt(LocalDateTime.now());

        participantRepo.save(participant);

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
    @Scheduled(fixedRate = 60000)
    public void autoSubmitExpiredExams() {

        LocalDateTime now = LocalDateTime.now();

        List<ExamParticipant> participants = participantRepo.findByCompletedFalse();

        for (ExamParticipant p : participants) {

            LocalDateTime expiredAt = p.getStartedAt().plusMinutes(p.getExam().getDuration());

            if (now.isAfter(expiredAt)) {

                List<ExamAnswer> savedAnswers = examAnswerRepository.findByParticipant_Id(p.getId());

                float totalScore = 0;

                for (ExamAnswer answer : savedAnswers) {
                    Question question = questionRepo.findById(answer.getQuestionId()).orElse(null);
                    if (question == null) continue;

                    boolean correct = question.getAnswer() != null &&
                            question.getAnswer().trim().equalsIgnoreCase(answer.getAnswer() != null ? answer.getAnswer().trim() : "");

                    answer.setIsCorrect(correct);
                    answer.setScore(correct ? 1f : 0f);

                    totalScore += answer.getScore() != null ? answer.getScore() : 0;

                    examAnswerRepository.save(answer);
                }

                p.setScore(totalScore);
                p.setCompleted(true);
                p.setFinishedAt(expiredAt);
                participantRepo.save(p);
            }
        }
    }
}
