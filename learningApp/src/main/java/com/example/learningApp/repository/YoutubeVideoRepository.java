package com.example.learningApp.repository;

import com.example.learningApp.entity.YoutubeVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface YoutubeVideoRepository extends JpaRepository<YoutubeVideo, String> {
    @Query("""
    select v
    from User u
    join u.savedVideos v
    where u.id = :userId
""")
    List<YoutubeVideo> findSavedVideosByUserId(@Param("userId") String userId);

}
