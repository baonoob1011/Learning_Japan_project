package com.example.learningApp.repository;

import com.example.learningApp.entity.ReviewSessionItem;
import com.example.learningApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReviewSessionItemRepository extends JpaRepository<ReviewSessionItem, String> {
    List<ReviewSessionItem> findBySession_IdOrderByOrderIndexAsc(String sessionId);

    @Query("SELECT i FROM ReviewSessionItem i JOIN FETCH i.wordProgress wp WHERE i.session.id = :sessionId ORDER BY i.orderIndex ASC")
    List<ReviewSessionItem> findBySession_IdWithProgress(String sessionId);

    Optional<ReviewSessionItem> findBySession_UserAndSession_DateAndWordProgress_Id(User user, LocalDate date, String wordProgressId);

    long countBySession_IdAndCompletedFalse(String sessionId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ReviewSessionItem i WHERE i.wordProgress.id = :wordProgressId")
    void deleteByWordProgress_Id(String wordProgressId);
}
