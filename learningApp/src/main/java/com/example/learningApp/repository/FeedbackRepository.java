package com.example.learningApp.repository;

import com.example.learningApp.entity.Feedback;
import com.example.learningApp.enums.FeedbackStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, String> {

    List<Feedback> findByUserId(String userId);

    List<Feedback> findByStatus(FeedbackStatus status);}