package com.example.learningApp.repository;

import com.example.learningApp.entity.YoutubeVideoDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface YoutubeVideoSearchRepository
        extends ElasticsearchRepository<YoutubeVideoDocument, String> {

    List<YoutubeVideoDocument>
    findByTitleContainingAndLevelAndVideoTag(
            String title,
            String level,
            String videoTag
    );
}
