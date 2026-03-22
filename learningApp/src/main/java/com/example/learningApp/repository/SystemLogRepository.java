package com.example.learningApp.repository;

import com.example.learningApp.entity.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface SystemLogRepository extends JpaRepository<SystemLog, String> {

    @Query("""
            SELECT s
            FROM SystemLog s
            WHERE (:keyword IS NULL OR
                   lower(s.targetClass) LIKE lower(concat('%', :keyword, '%')) OR
                   lower(s.methodName) LIKE lower(concat('%', :keyword, '%')) OR
                   lower(coalesce(s.errorMessage, '')) LIKE lower(concat('%', :keyword, '%')))
              AND (:username IS NULL OR
                   lower(coalesce(s.username, '')) LIKE lower(concat('%', :username, '%')))
              AND (:status IS NULL OR s.status = :status)
              AND (:fromTime IS NULL OR s.createdAt >= :fromTime)
              AND (:toTime IS NULL OR s.createdAt <= :toTime)
            ORDER BY s.createdAt DESC
            """)
    Page<SystemLog> searchLogs(
            @Param("keyword") String keyword,
            @Param("username") String username,
            @Param("status") String status,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime,
            Pageable pageable
    );
}
