package com.example.learningApp.service.exam;

import com.example.learningApp.dto.request.exam.SubmitExamRequest;
import com.example.learningApp.dto.response.exam.SubmitExamResponse;
import com.example.learningApp.entity.ExamAnswer;
import com.example.learningApp.entity.ExamParticipant;
import com.example.learningApp.entity.Question;
import com.example.learningApp.repository.ExamAnswerRepository;
import com.example.learningApp.repository.ExamParticipantRepository;
import com.example.learningApp.repository.QuestionRepository;
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

    @Transactional
    public SubmitExamResponse submitExam(SubmitExamRequest request) {

        ExamParticipant participant = participantRepo.findById(request.getParticipantId())
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));

        if (Boolean.TRUE.equals(participant.getCompleted())) {
            throw new IllegalStateException("Exam already submitted");
        }

        LocalDateTime expiredAt =
                participant.getStartedAt()
                        .plusMinutes(participant.getExam().getDuration());

        // ⛔ quá giờ thì không cho submit tay
        if (LocalDateTime.now().isAfter(expiredAt)) {
            throw new IllegalStateException("Exam time expired");
        }

        // ======================
        // 1️⃣ LẤY TOÀN BỘ ANSWER ĐÃ LƯU
        // ======================
        Map<String, String> answers =
                examAnswerRepository
                        .findAnswersByParticipantId(participant.getId())
                        .stream()
                        .collect(Collectors.toMap(
                                r -> (String) r[0], // questionId
                                r -> (String) r[1]  // answer
                        ));

        // ======================
        // 2️⃣ CHẤM ĐIỂM
        // ======================
        float score = calculateScore(
                participant.getExam().getId(),
                answers
        );

        // ======================
        // 3️⃣ HOÀN TẤT BÀI THI
        // ======================
        participant.setScore(score);
        participant.setCompleted(true);
        participant.setFinishedAt(LocalDateTime.now());

        participantRepo.save(participant);

        // ======================
        // 4️⃣ RESPONSE
        // ======================
        return SubmitExamResponse.builder()
                .participantId(participant.getId())
                .examId(participant.getExam().getId())
                .examCode(participant.getExam().getCode())
                .score(score)
                .completed(true)
                .startedAt(participant.getStartedAt())
                .finishedAt(participant.getFinishedAt())
                .build();
    }



    /**
     * Tính điểm đơn giản
     */
    private float calculateScore(String examId, Map<String, String> userAnswers) {

        float score = 0;

        for (Map.Entry<String, String> entry : userAnswers.entrySet()) {

            Question question = questionRepo
                    .findByIdAndExamId(entry.getKey(), examId);

            if (question == null) continue;

            // ❌ Không auto chấm ESSAY / READING
            if ("ESSAY".equals(question.getType())
                    || "READING".equals(question.getType())) {
                continue;
            }

            if (question.getAnswer() != null &&
                    question.getAnswer().trim()
                            .equalsIgnoreCase(entry.getValue().trim())) {
                score += 1;
            }
        }

        return score;
    }


    @Transactional
    @Scheduled(fixedRate = 60000)
    public void autoSubmitExpiredExams() {

        LocalDateTime now = LocalDateTime.now();

        List<ExamParticipant> participants =
                participantRepo.findByCompletedFalse();

        for (ExamParticipant p : participants) {

            LocalDateTime expiredAt =
                    p.getStartedAt()
                            .plusMinutes(p.getExam().getDuration());

            if (now.isAfter(expiredAt)) {

                // 🔥 LẤY ANSWERS ĐÃ LƯU
                Map<String, String> answers =
                        examAnswerRepository
                                .findAnswersByParticipantId(p.getId())
                                .stream()
                                .collect(Collectors.toMap(
                                        r -> (String) r[0],
                                        r -> (String) r[1]
                                ));

                float score = calculateScore(
                        p.getExam().getId(),
                        answers
                );

                p.setScore(score);
                p.setCompleted(true);
                p.setFinishedAt(expiredAt); // ⏰ đúng thời điểm hết giờ

                participantRepo.save(p);
            }
        }
    }



}
