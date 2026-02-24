package com.example.learningApp.repository;


import com.example.learningApp.entity.AnswerOption;
import com.example.learningApp.entity.QuestionVideoYoutube;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerOptionRepository extends JpaRepository<AnswerOption, String> {
    List<AnswerOption> findByQuestionId(String questionId);

}
