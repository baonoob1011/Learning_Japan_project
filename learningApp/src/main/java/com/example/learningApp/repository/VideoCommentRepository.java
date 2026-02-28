package com.example.learningApp.repository;

import com.example.learningApp.entity.VideoComment;
import com.example.learningApp.entity.YoutubeVideo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoCommentRepository extends JpaRepository<VideoComment, String> {

    List<VideoComment> findByVideoAndParentIsNullOrderByCreatedAtDesc(YoutubeVideo video);

    List<VideoComment> findByParentId(String parentId);
}