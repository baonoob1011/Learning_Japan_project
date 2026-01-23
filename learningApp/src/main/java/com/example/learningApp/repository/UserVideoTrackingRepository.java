package com.example.learningApp.repository;

import com.example.learningApp.entity.User;
import com.example.learningApp.entity.UserVideoTracking;
import com.example.learningApp.entity.YoutubeVideo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserVideoTrackingRepository extends JpaRepository<UserVideoTracking,String> {
    Optional<UserVideoTracking> findByUserAndVideo(User user, YoutubeVideo video);
    List<UserVideoTracking> findAllByUser(User user);

}
