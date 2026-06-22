package com.example.learningApp.repository;

import com.example.learningApp.entity.UserKanjiProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserKanjiProgressRepository
        extends JpaRepository<UserKanjiProgress, String> {

    Optional<UserKanjiProgress> findByUserIdAndKanjiId(
            String userId, String kanjiId
    );
}
