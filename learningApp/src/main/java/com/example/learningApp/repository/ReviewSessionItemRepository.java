package com.example.learningApp.repository;

import com.example.learningApp.entity.ReviewSessionItem;
import com.example.learningApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReviewSessionItemRepository extends JpaRepository<ReviewSessionItem, String> {
    List<ReviewSessionItem> findBySession_IdOrderByOrderIndexAsc(String sessionId);

    Optional<ReviewSessionItem> findBySession_UserAndSession_DateAndWordProgress_Id(User user, LocalDate date, String wordProgressId);

    long countBySession_IdAndCompletedFalse(String sessionId);
}

