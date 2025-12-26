package com.example.learningApp.repository;

import com.example.learningApp.entity.YoutubeVideo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface YoutubeVideoRepository extends JpaRepository<YoutubeVideo, String> {
}
