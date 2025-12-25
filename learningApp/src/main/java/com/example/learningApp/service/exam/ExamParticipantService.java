package com.example.learningApp.service.exam;

import com.example.learningApp.dto.cache.QuestionCache;
import com.example.learningApp.dto.cache.SectionCache;
import com.example.learningApp.dto.request.exam.StartExamRequest;
import com.example.learningApp.dto.response.exam.StartExamResponse;
import com.example.learningApp.entity.Exam;
import com.example.learningApp.entity.ExamParticipant;
import com.example.learningApp.entity.ExamSection;
import com.example.learningApp.entity.User;
import com.example.learningApp.enums.AssessmentType;
import com.example.learningApp.repository.ExamParticipantRepository;
import com.example.learningApp.repository.ExamRepository;
import com.example.learningApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExamParticipantService {

    private final ExamParticipantRepository participantRepo;
    private final ExamRepository examRepo;
    private final UserRepository userRepo;
    private final RedisTemplate<String, Object> redisTemplate;


    @Transactional
    public StartExamResponse startExam(StartExamRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        // ... kiểm tra quyền, số lần thi hàng ngày

        Exam exam = examRepo.findById(request.getExamId())
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Tạo participant
        ExamParticipant participant = ExamParticipant.builder()
                .exam(exam)
                .user(user)
                .startedAt(LocalDateTime.now())
                .completed(false)
                .build();
        participantRepo.save(participant);

        // **Tăng số lần tham gia exam**
        exam.setParticipant(exam.getParticipant() + 1); // tăng lên 1
        examRepo.save(exam);

        // ===== CACHE QUESTIONS + SECTIONS THEO EXAMID =====
        String examQuestionKey = "exam:" + exam.getId() + ":questions";
        String examSectionKey = "exam:" + exam.getId() + ":sections";

        if (Boolean.FALSE.equals(redisTemplate.hasKey(examQuestionKey))) {
            Map<String, QuestionCache> questionCacheMap = new HashMap<>();
            Map<String, SectionCache> sectionCacheMap = new HashMap<>();

            for (ExamSection section : exam.getSections()) {
                Map<AssessmentType, Float> pointMap = new HashMap<>();
                for (var item : section.getAssessmentItems()) {
                    pointMap.put(item.getAssessmentType(), item.getPointPerQuestion());
                }
                sectionCacheMap.put(section.getId(), new SectionCache(section.getId(), pointMap));

                for (var q : section.getQuestions()) {
                    questionCacheMap.put(q.getId(), new QuestionCache(
                            q.getId(),
                            q.getQuestionType(),
                            q.getAnswer(),
                            section.getId(),
                            q.getQuestionText(),
                            q.getOptions(),
                            q.getQuestionOrder(),
                            q.getAnswer(),
                            q.getExplanation(),
                            q.getImageUrl(),
                            q.getAudioUrl()
                    ));
                }
            }

            redisTemplate.opsForValue().set(examQuestionKey, questionCacheMap, Duration.ofHours(5));
            redisTemplate.opsForValue().set(examSectionKey, sectionCacheMap, Duration.ofHours(5));
        }

        return StartExamResponse.builder()
                .participantId(participant.getId())
                .examId(exam.getId())
                .examCode(exam.getCode())
                .duration(exam.getDuration())
                .userId(user.getId())
                .completed(false)
                .startedAt(participant.getStartedAt())
                .build();
    }



}

