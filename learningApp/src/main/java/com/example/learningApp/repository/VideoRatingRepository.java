package com.example.learningApp.repository;

import com.example.learningApp.entity.VideoRating;
import com.example.learningApp.entity.User;
import com.example.learningApp.entity.YoutubeVideo;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VideoRatingRepository extends JpaRepository<VideoRating, String> {
    @Query("SELECT vr FROM VideoRating vr WHERE vr.video = :video")
    List<VideoRating> findAllByVideo(@Param("video") YoutubeVideo video);

    Optional<VideoRating> findByUserAndVideo(User user, YoutubeVideo video);

    @Query("SELECT AVG(v.rating) FROM VideoRating v WHERE v.video = :video")
    Double getAverageRating(@Param("video") YoutubeVideo video);

    @Query("SELECT COUNT(v) FROM VideoRating v WHERE v.video = :video")
    Long countByVideo(@Param("video") YoutubeVideo video);
}
