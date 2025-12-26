package com.example.learningApp.component.kafka;


import com.example.learningApp.dto.request.progress.UpdateUserLearningProgressRequest;
import com.example.learningApp.dto.response.exam.UserExamResultResponse;
import com.example.learningApp.entity.UserExamResult;
import com.example.learningApp.service.exam.UserExamResultService;
import com.example.learningApp.service.progress.UserLearningProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Consumer {

    private final UserLearningProgressService progressService;
    private final UserExamResultService userExamResultService;

    @KafkaListener(
            topics = "user-learning-progress"
    )
    public void learningProgressConsume(UpdateUserLearningProgressRequest request) {
        progressService.updateProgressAfterExam(request);
    }

    @KafkaListener(
            topics = "user-exam-result"
    )
    public void examResultConsume(UserExamResultResponse userExamResult) {
        userExamResultService.saveResult(userExamResult);
    }
}
