package com.example.learningApp.service.exam;


import com.example.learningApp.dto.response.exam.UserExamResultResponse;
import com.example.learningApp.entity.Exam;
import com.example.learningApp.entity.User;
import com.example.learningApp.entity.UserExamResult;
import com.example.learningApp.repository.UserExamResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserExamResultService {

    private final UserExamResultRepository userExamResultRepository;

    /**
     * Lưu kết quả thi của user
     */
    @Transactional
    public UserExamResult saveResult(UserExamResultResponse examResultResponse) {
        // Build entity từ DTO
        UserExamResult result = UserExamResult.builder()
                .user(User.builder().id(examResultResponse.getUserId()).build()) // chỉ cần id
                .exam(Exam.builder().id(examResultResponse.getExamId()).build()) // chỉ cần id
                .totalQuestions(examResultResponse.getTotalQuestions())
                .correctQuestions(examResultResponse.getCorrectQuestions())
                .score(examResultResponse.getScore())
                .submittedAt(examResultResponse.getSubmittedAt() != null
                        ? examResultResponse.getSubmittedAt()
                        : LocalDateTime.now())
                .build();

        // Lưu entity vào DB
        return userExamResultRepository.save(result);
    }

}

