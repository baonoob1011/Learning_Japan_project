package com.example.learningApp.repository;

import com.example.learningApp.entity.AssessmentItem;
import com.example.learningApp.entity.ExamSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssessmentItemRepository extends JpaRepository<AssessmentItem, String> {
    List<AssessmentItem> findBySection(ExamSection section);
}