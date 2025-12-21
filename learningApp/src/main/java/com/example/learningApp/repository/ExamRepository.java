package com.example.learningApp.repository;

import com.example.learningApp.entity.Exam;
import com.example.learningApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamRepository extends JpaRepository<Exam,String> {
    boolean existsByCode(String code);
    Optional<Exam> findByCode(String code);

    @Query("SELECT e FROM Exam e WHERE LOWER(e.code) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(e.level) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Exam> searchByKeyword(@Param("keyword") String keyword);
}
