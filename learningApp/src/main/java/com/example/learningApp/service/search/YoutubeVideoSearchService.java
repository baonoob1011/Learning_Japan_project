package com.example.learningApp.service.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.example.learningApp.dto.response.video.YoutubeVideoSummaryResponse;
import com.example.learningApp.entity.YoutubeVideoDocument;
import com.example.learningApp.enums.JLPTLevel;
import com.example.learningApp.enums.VideoTag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class YoutubeVideoSearchService {

    private final ElasticsearchClient elasticsearchClient;

    public List<YoutubeVideoSummaryResponse> searchSuggest(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }

        final String q = keyword.trim().toLowerCase();

        try {
            var response = elasticsearchClient.search(s -> s
                            .index("youtube_videos")
                            .size(10)
                            .query(query -> query
                                    .bool(b -> b

                                            // 🔥 Search từng ký tự (R cũng ra)
                                            .should(sh -> sh.wildcard(w -> w
                                                    .field("title.keyword") // dùng keyword field
                                                    .value("*" + q + "*")
                                                    .caseInsensitive(true)
                                            ))

                                            // ✅ Giữ nguyên search cũ
                                            .should(sh -> sh.match(m -> m
                                                    .field("title")
                                                    .query(q)
                                            ))

                                            .minimumShouldMatch("1")
                                    )
                            ),
                    YoutubeVideoDocument.class
            );

            return response.hits()
                    .hits()
                    .stream()
                    .map(hit -> {
                        YoutubeVideoDocument doc = hit.source();
                        if (doc == null) return null;

                        return new YoutubeVideoSummaryResponse(
                                doc.getId(),
                                doc.getTitle(),
                                doc.getUrlVideo(),
                                parseVideoTag(doc.getVideoTag()),
                                parseLevel(doc.getLevel()),
                                doc.getDuration(),
                                doc.getCreatedAt() != null
                                        ? Instant.ofEpochMilli(doc.getCreatedAt())
                                        : Instant.now()
                        );
                    })
                    .filter(Objects::nonNull)
                    .toList();

        } catch (IOException e) {
            throw new RuntimeException("Search video failed", e);
        }
    }

    /* ===================== HELPERS ===================== */

    private VideoTag parseVideoTag(String value) {
        try {
            return value != null ? VideoTag.valueOf(value) : VideoTag.DEFAULT;
        } catch (Exception e) {
            return VideoTag.DEFAULT;
        }
    }

    private JLPTLevel parseLevel(String value) {
        try {
            return value != null ? JLPTLevel.valueOf(value) : JLPTLevel.UNKNOWN;
        } catch (Exception e) {
            return JLPTLevel.UNKNOWN;
        }
    }
}
