package com.example.learningApp.repository.auth;

import com.example.learningApp.entity.auth.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    Optional<UserSession> findBySessionId(String sessionId);

    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.userId = :userId AND s.isActive = true")
    void deactivateAllSessionsByUserId(@Param("userId") String userId);
}
