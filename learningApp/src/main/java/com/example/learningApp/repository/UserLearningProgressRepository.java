package com.example.learningApp.repository;

import com.example.learningApp.entity.UserLearningProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserLearningProgressRepository
        extends JpaRepository<UserLearningProgress, String> {

    Optional<UserLearningProgress> findByUserIdAndLevel(String userId, String level);

    List<UserLearningProgress> findByUserId(String userId);
    @Query("""
    SELECT
        function('DATE', uer.submittedAt),
        COUNT(uer.id),
        SUM(uer.totalQuestions),
        SUM(uer.correctQuestions)
    FROM UserExamResult uer
    WHERE uer.user.id = :userId
      AND uer.submittedAt >= :fromDate
    GROUP BY function('DATE', uer.submittedAt)
    ORDER BY function('DATE', uer.submittedAt)
""")
    List<Object[]> getDailyProgress(
            @Param("userId") String userId,
            @Param("fromDate") LocalDateTime fromDate
    );


    UserLearningProgress findFirstByUserIdOrderByLastExamAtDesc(String id);
}

