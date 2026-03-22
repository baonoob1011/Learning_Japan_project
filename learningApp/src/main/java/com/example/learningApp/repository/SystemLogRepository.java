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
                    WHERE (COALESCE(CAST(:keyword AS TEXT), '') = '' OR
                           CAST(s.target_class AS TEXT) ILIKE CONCAT('%', CAST(:keyword AS TEXT), '%') OR
                           CAST(s.method_name AS TEXT) ILIKE CONCAT('%', CAST(:keyword AS TEXT), '%') OR
                           CAST(COALESCE(s.error_message, '') AS TEXT) ILIKE CONCAT('%', CAST(:keyword AS TEXT), '%'))
                      AND (COALESCE(CAST(:username AS TEXT), '') = '' OR
                           CAST(COALESCE(s.username, '') AS TEXT) ILIKE CONCAT('%', CAST(:username AS TEXT), '%'))
                      AND (COALESCE(CAST(:status AS TEXT), '') = '' OR CAST(s.status AS TEXT) = CAST(:status AS TEXT))
                      AND (CAST(:fromTime AS TIMESTAMP) IS NULL OR s.created_at >= CAST(:fromTime AS TIMESTAMP))
                      AND (CAST(:toTime AS TIMESTAMP) IS NULL OR s.created_at <= CAST(:toTime AS TIMESTAMP))
                    ORDER BY s.created_at DESC
                    """,
            countQuery = """
                    SELECT COUNT(1)
                    FROM system_logs s
                    WHERE (COALESCE(CAST(:keyword AS TEXT), '') = '' OR
                           CAST(s.target_class AS TEXT) ILIKE CONCAT('%', CAST(:keyword AS TEXT), '%') OR
                           CAST(s.method_name AS TEXT) ILIKE CONCAT('%', CAST(:keyword AS TEXT), '%') OR
                           CAST(COALESCE(s.error_message, '') AS TEXT) ILIKE CONCAT('%', CAST(:keyword AS TEXT), '%'))
                      AND (COALESCE(CAST(:username AS TEXT), '') = '' OR
                           CAST(COALESCE(s.username, '') AS TEXT) ILIKE CONCAT('%', CAST(:username AS TEXT), '%'))
                      AND (COALESCE(CAST(:status AS TEXT), '') = '' OR CAST(s.status AS TEXT) = CAST(:status AS TEXT))
                      AND (CAST(:fromTime AS TIMESTAMP) IS NULL OR s.created_at >= CAST(:fromTime AS TIMESTAMP))
                      AND (CAST(:toTime AS TIMESTAMP) IS NULL OR s.created_at <= CAST(:toTime AS TIMESTAMP))
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
