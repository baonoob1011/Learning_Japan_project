package com.example.learningApp.service.video;

import com.example.learningApp.entity.YoutubeVideo;
import com.example.learningApp.entity.YoutubeVideoDocument;
import com.example.learningApp.repository.YoutubeVideoSearchRepository;
import com.example.learningApp.repository.YoutubeVideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class YoutubeVideoSyncService {

    private final YoutubeVideoRepository youtubeVideoRepository;
    private final YoutubeVideoSearchRepository documentRepository;

    @Transactional(readOnly = true)
    public void syncIfNotExists() {

        log.info("🔄 Start syncing YoutubeVideo DB → Elasticsearch");

        youtubeVideoRepository.findAll().forEach(video -> {

            boolean exists = documentRepository.existsById(video.getId());
            if (exists) {
                log.debug("⏭ Video {} already indexed", video.getId());
                return;
            }

            YoutubeVideoDocument doc = mapToDocument(video);
            documentRepository.save(doc);

            log.info("✅ Indexed video {}", video.getId());
        });

        log.info("🎯 Sync YoutubeVideo completed");
    }

    private YoutubeVideoDocument mapToDocument(YoutubeVideo video) {
        return YoutubeVideoDocument.builder()
                .id(video.getId())
                .title(video.getTitle())
                .urlVideo(video.getUrlVideo())
                .videoTag(video.getVideoTag() != null
                        ? video.getVideoTag().name()
                        : null)
                .level(video.getLevel() != null
                        ? video.getLevel().name()
                        : null)
                .duration(video.getDuration())
                .createdAt(
                        video.getCreatedAt() != null
                                ? video.getCreatedAt().toEpochMilli()
                                : null
                )
                .build();
    }
}
