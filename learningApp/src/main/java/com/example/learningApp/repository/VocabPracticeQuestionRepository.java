package com.example.learningApp.repository;

import com.example.learningApp.entity.Vocab;
import com.example.learningApp.entity.VocabPracticeQuestion;
import com.example.learningApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VocabPracticeQuestionRepository extends JpaRepository<VocabPracticeQuestion, String> {
    List<VocabPracticeQuestion> findByUser(User user);
    List<VocabPracticeQuestion> findByUserAndVocab(User user, Vocab vocab);
    Optional<VocabPracticeQuestion> findFirstByUserAndVocabOrderByCreatedAtDesc(User user, Vocab vocab);
}
