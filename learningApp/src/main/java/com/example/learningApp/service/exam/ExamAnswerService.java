package com.example.learningApp.service.exam;

import com.example.learningApp.dto.request.exam.SaveAnswerRequest;
import com.example.learningApp.entity.ExamAnswer;
import com.example.learningApp.entity.ExamParticipant;
import com.example.learningApp.entity.Question;
import com.example.learningApp.repository.ExamAnswerRepository;
import com.example.learningApp.repository.ExamParticipantRepository;
import com.example.learningApp.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ExamAnswerService {

    private final ExamAnswerRepository answerRepo;
    private final ExamParticipantRepository participantRepo;
    private final QuestionRepository questionRepo;

    @Transactional
    public void saveAnswer(SaveAnswerRequest request) {

        ExamParticipant participant = participantRepo.findById(request.getParticipantId())
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));

        if (Boolean.TRUE.equals(participant.getCompleted())) {
            throw new IllegalStateException("Exam already completed");
        }

        Question question = questionRepo.findById(request.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        // 🔁 Nếu đã trả lời → update
        ExamAnswer answer = answerRepo
                .findByParticipant_IdAndQuestion_Id(
                        participant.getId(),
                        question.getId()
                )
                .orElseGet(() -> {
                    ExamAnswer a = new ExamAnswer();
                    a.setParticipant(participant);
                    a.setQuestion(question);
                    a.setCreatedAt(LocalDateTime.now());
                    return a;
                });

        answer.setAnswer(request.getAnswer());

        answerRepo.save(answer);
    }
}
