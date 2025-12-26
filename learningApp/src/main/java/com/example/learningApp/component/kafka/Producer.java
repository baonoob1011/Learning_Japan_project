package com.example.learningApp.component.kafka;


import com.example.learningApp.dto.request.progress.UpdateUserLearningProgressRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Producer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(String topic,String userId, Object request) {
        kafkaTemplate.send(topic, userId, request);
    }
}
