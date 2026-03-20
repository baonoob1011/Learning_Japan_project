package com.example.learningApp.service.progress;

import com.example.learningApp.dto.response.progress.AdminUserProgressResponse;
import com.example.learningApp.dto.response.progress.DailyProgressResponse;
import com.example.learningApp.dto.response.progress.LevelDetailResponse;
import com.example.learningApp.dto.response.user.UserResponse;
import com.example.learningApp.entity.User;
import com.example.learningApp.entity.UserLearningProgress;
import com.example.learningApp.mapper.UserMapper;
import com.example.learningApp.repository.UserLearningProgressRepository;
import com.example.learningApp.repository.UserRepository;
import com.example.learningApp.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminLearningService {
    private final UserLearningProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;


    public AdminUserProgressResponse getUserProgress(String userId) {
        // 1. Lấy User trước (để đảm bảo user tồn tại)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserResponse userResponse = userMapper.toUserResponse(user);

        // 2. Lấy list progress
        List<UserLearningProgress> progressList = progressRepository.findByUserId(userId);

        // 3. Khởi tạo biến (Mặc định là 0/null/empty)
        int totalExams = 0;
        int totalQuestions = 0;
        int totalCorrect = 0;
        String lastLevelName = null; // Mặc định null
        List<LevelDetailResponse> levelDetails = new ArrayList<>();

        // 4. Nếu có dữ liệu thì mới tính toán
        if (!progressList.isEmpty()) {
            UserLearningProgress lastActiveProgress = null;

            for (UserLearningProgress p : progressList) {
                totalExams += p.getTotalExamsTaken();
                totalQuestions += p.getTotalQuestionsDone();
                totalCorrect += p.getCorrectQuestions();

                // Tìm level gần nhất
                if (lastActiveProgress == null ||
                        (p.getLastExamAt() != null && p.getLastExamAt().isAfter(lastActiveProgress.getLastExamAt()))) {
                    lastActiveProgress = p;
                }

                // Map level detail
                double levelAccuracy = (p.getTotalQuestionsDone() > 0)
                        ? (double) p.getCorrectQuestions() / p.getTotalQuestionsDone() * 100
                        : 0.0;

                levelDetails.add(LevelDetailResponse.builder()
                        .level(p.getLevel())
                        .totalExamsTaken(p.getTotalExamsTaken())
                        .totalQuestionsDone(p.getTotalQuestionsDone())
                        .correctQuestions(p.getCorrectQuestions())
                        .accuracy(levelAccuracy)
                        .lastExamAt(p.getLastExamAt())
                        .build());
            }

            // Gán tên level cuối cùng nếu tìm thấy
            if (lastActiveProgress != null) {
                lastLevelName = lastActiveProgress.getLevel();
            }
        }

        // 5. Tính độ chính xác tổng (tránh chia cho 0)
        double totalAccuracy = (totalQuestions > 0)
                ? (double) totalCorrect / totalQuestions * 100
                : 0.0;

        // 6. Return duy nhất 1 chỗ (Luôn có userResponse)
        return AdminUserProgressResponse.builder()
                .totalExamsTaken(totalExams)
                .totalQuestionsDone(totalQuestions)
                .correctQuestions(totalCorrect)
                .accuracy(totalAccuracy)
                .lastLevel(lastLevelName)
                .levels(levelDetails)
                .user(userResponse) // Luôn luôn trả về user
                .build();
    }

    @Transactional(readOnly = true)
    public List<DailyProgressResponse> getDailyProgress(String userId, int days) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime fromDate =
                LocalDate.now().minusDays(days - 1).atStartOfDay();

        List<Object[]> rawData =
                progressRepository.getDailyProgress(userId, fromDate);

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

            float accuracy = totalQuestions == 0
                    ? 0f
                    : (correct * 100f / totalQuestions);

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

