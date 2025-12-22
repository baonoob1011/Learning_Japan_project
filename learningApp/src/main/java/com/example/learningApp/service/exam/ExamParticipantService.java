package com.example.learningApp.service.exam;

import com.example.learningApp.dto.request.exam.StartExamRequest;
import com.example.learningApp.dto.response.exam.StartExamResponse;
import com.example.learningApp.entity.Exam;
import com.example.learningApp.entity.ExamParticipant;
import com.example.learningApp.entity.User;
import com.example.learningApp.repository.ExamParticipantRepository;
import com.example.learningApp.repository.ExamRepository;
import com.example.learningApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExamParticipantService {

    private final ExamParticipantRepository participantRepo;
    private final ExamRepository examRepo;
    private final UserRepository userRepo;

    public StartExamResponse startExam(StartExamRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Lấy userId trực tiếp từ principal
        String userId = authentication.getName(); // thường là username / sub

        // 🔹 Lấy JWT claims
        Map<String, Object> claims = ((JwtAuthenticationToken) authentication)
                .getTokenAttributes();

        List<String> groups = (List<String>) claims.getOrDefault("cognito:groups", List.of());

        boolean isVip = groups.contains("USER_VIP");
        boolean isNormalUser = groups.contains("USER");

        // 🔒 USER thường → tối đa 3 lần / exam / ngày
        if (isNormalUser && !isVip) {

            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

            long attemptCount = participantRepo.countByUser_IdAndExam_IdAndStartedAtBetween(
                    userId,
                    request.getExamId(),
                    startOfDay,
                    endOfDay
            );

            if (attemptCount >= 3) {
                throw new IllegalStateException(
                        "You can only take this exam 3 times per day"
                );
            }
        }

        Exam exam = examRepo.findById(request.getExamId())
                .orElseThrow(() -> new IllegalArgumentException("Exam not found"));

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ExamParticipant participant = ExamParticipant.builder()
                .exam(exam)
                .user(user)
                .startedAt(LocalDateTime.now())
                .completed(false)
                .build();

        participantRepo.save(participant);

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

