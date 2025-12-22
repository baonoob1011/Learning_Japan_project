package com.example.learningApp.service.exam;

import com.example.learningApp.dto.cache.ExamAnswerCache;
import com.example.learningApp.dto.request.exam.SaveAnswerRequest;
import com.example.learningApp.entity.ExamAnswer;
import com.example.learningApp.entity.ExamParticipant;
import com.example.learningApp.entity.Question;
import com.example.learningApp.repository.ExamAnswerRepository;
import com.example.learningApp.repository.ExamParticipantRepository;
import com.example.learningApp.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExamAnswerService {

    private final ExamParticipantRepository participantRepo;
    private final QuestionRepository questionRepo;
    private final RedisTemplate<String,Object> redisTemplate;

    @Transactional
    public void saveAnswer(SaveAnswerRequest request) {

        // 1️⃣ Lấy participant
        ExamParticipant participant = participantRepo.findById(request.getParticipantId())
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));

        if (Boolean.TRUE.equals(participant.getCompleted())) {
            throw new IllegalStateException("Exam already completed");
        }

        // 2️⃣ Lấy question
        Question question = questionRepo.findById(request.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        // 3️⃣ Redis key
        String redisKey = "exam:participant:" + participant.getId() + ":answers";

        // 4️⃣ Lấy snapshot hiện tại từ Redis
        Map<String, ExamAnswerCache> answersMap = (Map<String, ExamAnswerCache>) redisTemplate.opsForValue().get(redisKey);
        if (answersMap == null) {
            answersMap = new HashMap<>();
        }

        // 5️⃣ Update hoặc tạo mới snapshot
        ExamAnswerCache cache = answersMap.getOrDefault(question.getId(), new ExamAnswerCache());
        cache.setQuestionId(question.getId());
        cache.setQuestionText(question.getQuestionText());
        cache.setQuestionType(question.getQuestionType());
        cache.setOptions(question.getOptions());
        cache.setOrderNum(question.getOrderNum());
        cache.setCorrectAnswer(question.getAnswer());
        cache.setAnswer(request.getAnswer());
        cache.setAnsweredAt(LocalDateTime.now());

        answersMap.put(question.getId(), cache);

        // 6️⃣ Lưu lại Redis
        redisTemplate.opsForValue().set(redisKey, answersMap, Duration.ofHours(5));
    }
    
}
