package com.example.learningApp.common.kafka;

import com.example.learningApp.dto.request.progress.UpdateUserLearningProgressRequest;
import com.example.learningApp.dto.request.vocab.CreateVocabRequest;
import com.example.learningApp.dto.response.exam.UserExamResultResponse;
import com.example.learningApp.entity.UserExamResult;
import com.example.learningApp.entity.Vocab;
import com.example.learningApp.service.exam.UserExamResultService;
import com.example.learningApp.service.progress.UserLearningProgressService;
import com.example.learningApp.service.vocab.VocabService;
import com.example.learningApp.service.vocab.VocabExerciseService;
import com.example.learningApp.service.vocab.VocabEnrichmentService;
import com.example.learningApp.repository.UserRepository;
import com.example.learningApp.repository.VocabRepository;
import com.example.learningApp.dto.event.VocabSaveExerciseEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Consumer {

    private final VocabExerciseService vocabExerciseService;
    private final UserRepository userRepository;
    private final VocabRepository vocabRepository;
    private final UserLearningProgressService progressService;
    private final UserExamResultService userExamResultService;
    private final VocabService vocabService;

    private final VocabEnrichmentService enrichmentService;

    private static final String VOCAB_TOPIC = "create-vocab";
    private static final String PROGRESS_TOPIC = "user-learning-progress";
    private static final String EXAM_RESULT_TOPIC = "user-exam-result";
    private static final String VOCAB_SAVE_EXERCISE_TOPIC = "vocab-save-exercise";
    private static final String VOCAB_ENRICH_TOPIC = "enrich-vocab";

    @KafkaListener(topics = VOCAB_ENRICH_TOPIC)
    public void enrichVocab(String vocabId) {
        enrichmentService.enrichVocab(vocabId);
    }

    @KafkaListener(topics = PROGRESS_TOPIC)
    public void learningProgressConsume(UpdateUserLearningProgressRequest request) {
        progressService.updateProgressAfterExam(request);
    }

    @KafkaListener(topics = EXAM_RESULT_TOPIC)
    public void examResultConsume(UserExamResultResponse userExamResult) {
        userExamResultService.saveResult(userExamResult);
    }

    @KafkaListener(topics = VOCAB_TOPIC)
    public void createVocab(CreateVocabRequest request) {
        try {
            vocabService.createVocab(request);
        } catch (Exception e) {
            // log error và bỏ qua để không retry
            System.err.println("Error processing vocab: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = VOCAB_SAVE_EXERCISE_TOPIC)
    public void vocabSaveExerciseConsume(VocabSaveExerciseEvent event) {
        try {
            var user = userRepository.findById(event.getUserId()).orElse(null);
            var vocab = vocabRepository.findById(event.getVocabId()).orElse(null);
            if (user != null && vocab != null) {
                vocabExerciseService.generateAndSaveQuestion(user, vocab);
            }
        } catch (Exception e) {
            System.err.println("Error processing vocab save exercise: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
