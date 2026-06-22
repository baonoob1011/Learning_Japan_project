package com.example.learningApp.repository;

import com.example.learningApp.entity.Kanji;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KanjiRepository extends JpaRepository<Kanji, String> {
}

