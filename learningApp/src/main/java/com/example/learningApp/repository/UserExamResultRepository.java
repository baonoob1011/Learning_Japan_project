package com.example.learningApp.repository;

import com.example.learningApp.entity.UserExamResult;
import com.example.learningApp.entity.UserLearningProgress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserExamResultRepository extends JpaRepository<UserExamResult, String> {
    void deleteByExam_Id(String examId);
}
