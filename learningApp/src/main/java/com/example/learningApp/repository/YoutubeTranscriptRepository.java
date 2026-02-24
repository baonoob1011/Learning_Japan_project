package com.example.learningApp.repository;

import com.example.learningApp.entity.YoutubeTranscript;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface YoutubeTranscriptRepository extends JpaRepository<YoutubeTranscript, String> {
    List<YoutubeTranscript> findByVideoId(String videoId);
}
