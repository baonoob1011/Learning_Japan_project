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

        // 🔁 Nếu đã trả lời → update, dùng participantId + questionId snapshot
        ExamAnswer answer = answerRepo
                .findByParticipant_IdAndQuestionId(
                        participant.getId(),
                        question.getId()
                )
                .orElseGet(() -> {
                    ExamAnswer a = new ExamAnswer();
                    a.setParticipant(participant);

                    // 🔥 SNAPSHOT QUESTION
                    a.setQuestionId(question.getId());
                    a.setQuestionText(question.getQuestionText());
                    a.setQuestionType(question.getType());
                    a.setOptions(question.getOptions());
                    a.setOrderNum(question.getOrderNum());
                    a.setCorrectAnswer(question.getAnswer());

                    return a;
                });

        // Gán câu trả lời của user và thời điểm trả lời
        answer.setAnswer(request.getAnswer());
        answer.setAnsweredAt(LocalDateTime.now());

        answerRepo.save(answer);
    }


}
