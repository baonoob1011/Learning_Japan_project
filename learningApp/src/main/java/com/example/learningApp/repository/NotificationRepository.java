package com.example.learningApp.repository;

import com.example.learningApp.entity.Notification;
import com.example.learningApp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    Page<Notification> findByUser(User user, Pageable pageable);

    Optional<Notification> findByIdAndUser(String id, User user);

    boolean existsByUserAndTitleContainingAndCreatedAtAfter(User user, String titlePart, LocalDateTime after);

    boolean existsByUserAndTitleContainingAndIsReadFalse(User user, String titlePart);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND n.isRead = false")
    long countByUserAndIsReadFalse(@Param("user") User user);

    @Modifying
    @Query("""
                update Notification n
                set n.isRead = true
                where n.user = :user and n.isRead = false
            """)
    void markAllAsReadByUser(@Param("user") User user);

    @Modifying
    void deleteAllByUser(User user);
}
