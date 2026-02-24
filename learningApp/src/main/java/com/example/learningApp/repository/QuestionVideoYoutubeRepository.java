package com.example.learningApp.repository;


import com.example.learningApp.entity.Exercise;
import com.example.learningApp.entity.QuestionVideoYoutube;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionVideoYoutubeRepository extends JpaRepository<QuestionVideoYoutube, String> {
    List<QuestionVideoYoutube> findByExerciseId(String exerciseId);

}
