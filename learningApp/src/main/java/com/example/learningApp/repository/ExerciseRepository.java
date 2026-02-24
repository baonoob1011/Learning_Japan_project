package com.example.learningApp.repository;


import com.example.learningApp.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExerciseRepository extends JpaRepository<Exercise, String> {
    Optional<Exercise> findFirstByVideoIdOrderByCreatedAtDesc(String videoId);
}
