package com.example.learningApp.repository;

import com.example.learningApp.entity.Exam;
import com.example.learningApp.entity.ExamSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamSectionRepository extends JpaRepository<ExamSection,String> {
    List<ExamSection> findByExamId(String examId);
}
