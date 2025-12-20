package com.example.learningApp.repository;

import com.example.learningApp.entity.Exam;
import com.example.learningApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExamRepository extends JpaRepository<Exam,String> {
    boolean existsByCode(String code);
}
