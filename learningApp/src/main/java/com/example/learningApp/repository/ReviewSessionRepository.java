package com.example.learningApp.repository;

import com.example.learningApp.entity.ReviewSession;
import com.example.learningApp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ReviewSessionRepository extends JpaRepository<ReviewSession, String> {
    Optional<ReviewSession> findByUserAndDate(User user, LocalDate date);

    Page<ReviewSession> findByUserOrderByDateDesc(User user, Pageable pageable);
}

