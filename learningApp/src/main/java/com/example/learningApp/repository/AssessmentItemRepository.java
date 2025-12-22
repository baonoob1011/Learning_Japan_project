package com.example.learningApp.repository;

import com.example.learningApp.entity.AssessmentItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentItemRepository extends JpaRepository<AssessmentItem, String> {
}