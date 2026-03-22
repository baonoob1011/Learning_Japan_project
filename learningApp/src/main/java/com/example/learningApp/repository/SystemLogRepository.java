package com.example.learningApp.repository;

import com.example.learningApp.entity.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface SystemLogRepository extends JpaRepository<SystemLog, String> {

    @Query(
            value = """
                    SELECT *
                    FROM system_logs s
                    WHERE (:keyword IS NULL OR :keyword = '' OR
                           CAST(s.target_class AS TEXT) ILIKE CONCAT('%', :keyword, '%') OR
                           CAST(s.method_name AS TEXT) ILIKE CONCAT('%', :keyword, '%') OR
                           CAST(COALESCE(s.error_message, '') AS TEXT) ILIKE CONCAT('%', :keyword, '%'))
                      AND (:username IS NULL OR :username = '' OR
                           CAST(COALESCE(s.username, '') AS TEXT) ILIKE CONCAT('%', :username, '%'))
                      AND (:status IS NULL OR :status = '' OR CAST(s.status AS TEXT) = :status)
                      AND (:fromTime IS NULL OR s.created_at >= :fromTime)
                      AND (:toTime IS NULL OR s.created_at <= :toTime)
                    ORDER BY s.created_at DESC
                    """,
            countQuery = """
                    SELECT COUNT(1)
                    FROM system_logs s
                    WHERE (:keyword IS NULL OR :keyword = '' OR
                           CAST(s.target_class AS TEXT) ILIKE CONCAT('%', :keyword, '%') OR
                           CAST(s.method_name AS TEXT) ILIKE CONCAT('%', :keyword, '%') OR
                           CAST(COALESCE(s.error_message, '') AS TEXT) ILIKE CONCAT('%', :keyword, '%'))
                      AND (:username IS NULL OR :username = '' OR
                           CAST(COALESCE(s.username, '') AS TEXT) ILIKE CONCAT('%', :username, '%'))
                      AND (:status IS NULL OR :status = '' OR CAST(s.status AS TEXT) = :status)
                      AND (:fromTime IS NULL OR s.created_at >= :fromTime)
                      AND (:toTime IS NULL OR s.created_at <= :toTime)
                    """,
            nativeQuery = true
    )
    Page<SystemLog> searchLogs(
            @Param("keyword") String keyword,
            @Param("username") String username,
            @Param("status") String status,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime,
            Pageable pageable
    );
}
