package com.example.learningApp.service.progress;


import com.example.learningApp.dto.request.progress.UpdateUserLearningProgressRequest;
import com.example.learningApp.dto.response.progress.DailyProgressResponse;
import com.example.learningApp.dto.response.progress.UserLearningDashboardResponse;
import com.example.learningApp.dto.response.progress.UserLearningProgressResponse;
import com.example.learningApp.entity.User;
import com.example.learningApp.entity.UserLearningProgress;
import com.example.learningApp.mapper.UserLearningProgressMapper;
import com.example.learningApp.repository.UserExamResultRepository;
import com.example.learningApp.repository.UserLearningProgressRepository;
import com.example.learningApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserLearningProgressService {

    private final UserLearningProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final UserLearningProgressMapper userLearningProgressMapper;

    /**
     * Lấy toàn bộ progress của user (theo các level)
     */
    @Transactional(readOnly = true)
    public List<UserLearningProgressResponse> getProgressByUser(String userId) {

        return progressRepository.findByUserId(userId)
                .stream().map(userLearningProgressMapper::toUserLearningProgressResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update progress sau khi submit exam
     */
    @Transactional
    public void updateProgressAfterExam(UpdateUserLearningProgressRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserLearningProgress progress =
                progressRepository
                        .findByUserIdAndLevel(request.getUserId(), request.getLevel())
                        .orElseGet(() -> UserLearningProgress.builder()
                                .user(user)
                                .level(request.getLevel())
                                .totalExamsTaken(0)
                                .totalQuestionsDone(0)
                                .correctQuestions(0)
                                .averageScore(0f)
                                .build()
                        );

        // ===== SAFE DEFAULT =====
        int totalQuestions = request.getTotalQuestions() != null
                ? request.getTotalQuestions()
                : 0;

        int correctQuestions = request.getCorrectQuestions() != null
                ? request.getCorrectQuestions()
                : 0;

        // ===== UPDATE =====
        progress.setTotalExamsTaken(progress.getTotalExamsTaken() + 1);
        progress.setTotalQuestionsDone(
                progress.getTotalQuestionsDone() + totalQuestions
        );
        progress.setCorrectQuestions(
                progress.getCorrectQuestions() + correctQuestions
        );

        // ===== CALCULATE AVG (SAFE) =====
        if (progress.getTotalQuestionsDone() > 0) {
            float avg = (float) progress.getCorrectQuestions()
                    / progress.getTotalQuestionsDone() * 100;
            progress.setAverageScore(avg);
        } else {
            progress.setAverageScore(0f);
        }

        progress.setLastExamAt(LocalDateTime.now());

        progressRepository.save(progress);
    }
    public UserLearningDashboardResponse getDashboard() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserLearningProgress> progresses =
                progressRepository.findByUserId(userId);

        if (progresses.isEmpty()) {
            return UserLearningDashboardResponse.builder()
                    .userId(userId)
                    .accuracy(0f)
                    .build();
        }

        int totalExams = progresses.stream()
                .mapToInt(UserLearningProgress::getTotalExamsTaken)
                .sum();

        int totalQuestions = progresses.stream()
                .mapToInt(UserLearningProgress::getTotalQuestionsDone)
                .sum();

        int correctQuestions = progresses.stream()
                .mapToInt(UserLearningProgress::getCorrectQuestions)
                .sum();

        float accuracy = totalQuestions == 0
                ? 0
                : (float) correctQuestions / totalQuestions * 100;

        UserLearningProgress latest =
                progresses.stream()
                        .filter(p -> p.getLastExamAt() != null)
                        .max(Comparator.comparing(UserLearningProgress::getLastExamAt))
                        .orElse(null);

        return UserLearningDashboardResponse.builder()
                .userId(userId)
                .totalExamsTaken(totalExams)
                .totalQuestionsDone(totalQuestions)
                .correctQuestions(correctQuestions)
                .accuracy(accuracy)
                .lastLevel(latest != null ? latest.getLevel() : null)
                .lastExamAt(latest != null ? latest.getLastExamAt() : null)
                .levels(
                        progresses.stream().map(p ->
                                UserLearningDashboardResponse.LevelProgressDto.builder()
                                        .level(p.getLevel())
                                        .totalExamsTaken(p.getTotalExamsTaken())
                                        .totalQuestionsDone(p.getTotalQuestionsDone())
                                        .correctQuestions(p.getCorrectQuestions())
                                        .averageScore(p.getAverageScore())
                                        .accuracy(
                                                p.getTotalQuestionsDone() == 0
                                                        ? 0
                                                        : (float) p.getCorrectQuestions()
                                                        / p.getTotalQuestionsDone() * 100
                                        )
                                        .lastExamAt(p.getLastExamAt())
                                        .build()
                        ).toList()
                )
                .build();
    }
    @Transactional(readOnly = true)
    public List<DailyProgressResponse> getDailyProgress(int days) {

        // Lấy user hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Chuyển LocalDate -> LocalDateTime
        LocalDateTime fromDate = LocalDate.now().minusDays(days - 1).atStartOfDay();

        // Query từ repository
        List<Object[]> rawData = progressRepository.getDailyProgress(userId, fromDate);

        // Map sang DTO
        return rawData.stream().map(row -> {
            LocalDate date;
            Object rawDate = row[0];
            if (rawDate instanceof java.sql.Date) {
                date = ((java.sql.Date) rawDate).toLocalDate();
            } else if (rawDate instanceof LocalDate) {
                date = (LocalDate) rawDate;
            } else {
                throw new RuntimeException("Unknown date type: " + rawDate.getClass());
            }

            int exams = ((Number) row[1]).intValue();
            int totalQuestions = ((Number) row[2]).intValue();
            int correct = ((Number) row[3]).intValue();

            float accuracy = totalQuestions == 0 ? 0f : (correct * 100f / totalQuestions);

            return DailyProgressResponse.builder()
                    .date(date)
                    .totalExamsTaken(exams)
                    .totalQuestionsDone(totalQuestions)
                    .correctQuestions(correct)
                    .accuracy(accuracy)
                    .build();
        }).toList();
    }

}
