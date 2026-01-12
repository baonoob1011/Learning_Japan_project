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

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,String> {

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

    void deleteByEmail(String email);
}
