package com.example.learningApp.repository;

import com.example.learningApp.entity.AssessmentItem;
import com.example.learningApp.entity.ExamSection;
import com.example.learningApp.enums.AssessmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssessmentItemRepository extends JpaRepository<AssessmentItem, String> {
    List<AssessmentItem> findBySection(ExamSection section);

    List<AssessmentItem> findByLevel(String level);

    boolean existsBySectionAndAssessmentType(ExamSection section, AssessmentType assessmentType);

    java.util.Optional<AssessmentItem> findBySectionAndAssessmentType(ExamSection section, AssessmentType assessmentType);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM AssessmentItem a WHERE a.level = :level")
    void deleteByLevel(@org.springframework.data.repository.query.Param("level") String level);
}
