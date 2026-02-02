package com.example.learningApp.common.kafka;


import com.example.learningApp.dto.request.progress.UpdateUserLearningProgressRequest;
import com.example.learningApp.dto.request.vocab.CreateVocabRequest;
import com.example.learningApp.dto.response.exam.UserExamResultResponse;
import com.example.learningApp.entity.UserExamResult;
import com.example.learningApp.entity.Vocab;
import com.example.learningApp.service.exam.UserExamResultService;
import com.example.learningApp.service.progress.UserLearningProgressService;
import com.example.learningApp.service.vocab.VocabService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Consumer {

    private final UserLearningProgressService progressService;
    private final UserExamResultService userExamResultService;
    private final VocabService vocabService;
    private static final String VOCAB_TOPIC = "create-vocab";
    private static final String PROGRESS_TOPIC = "user-learning-progress";
    private static final String EXAM_RESULT_TOPIC = "user-exam-result";
    @KafkaListener(
            topics = PROGRESS_TOPIC
    )
    public void learningProgressConsume(UpdateUserLearningProgressRequest request) {
        progressService.updateProgressAfterExam(request);
    }

    @KafkaListener(
            topics = EXAM_RESULT_TOPIC
    )
    public void examResultConsume(UserExamResultResponse userExamResult) {
        userExamResultService.saveResult(userExamResult);
    }
    @KafkaListener(
            topics = VOCAB_TOPIC
    )
    public void createVocab(CreateVocabRequest request) {
        try {
            vocabService.createVocab(request);
        } catch (Exception e) {
            // log error và bỏ qua để không retry
            System.err.println("Error processing vocab: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
