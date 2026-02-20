package com.example.learningApp.repository;

import com.example.learningApp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,String> {
    List<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndEnabledTrue(
            String fullName,
            String email,
            Pageable pageable
    );
    @Modifying
    @Transactional
    @Query(
            value = """
            UPDATE user_roles
            SET user_id = :newUserId
            WHERE user_id = :oldUserId
        """,
            nativeQuery = true
    )
    void updateUserRoleUserId(
            @Param("oldUserId") String oldUserId,
            @Param("newUserId") String newUserId
    );    Optional<User> findByEmail(String email);

    Page<User> findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(String email, String fullName, Pageable pageable);
    List<User> findByVipExpiredAtBefore(LocalDateTime time);

    @Query(
            value = """
                    SELECT 
                        COUNT(*) AS total_users,
                        SUM(CASE WHEN enabled = true THEN 1 ELSE 0 END) AS active_users,
                        SUM(CASE WHEN enabled = false THEN 1 ELSE 0 END) AS inactive_users
                    FROM users
                    """,
            nativeQuery = true
    )
    Object getUserStatisticsRaw();
}
