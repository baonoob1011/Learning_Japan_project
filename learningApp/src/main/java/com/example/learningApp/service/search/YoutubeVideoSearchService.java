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

        try {
            var response = elasticsearchClient.search(s -> s
                            .index("youtube_videos")
                            .size(10)
                            .query(q -> q.bool(b -> {

                                // 🔥 AUTOCOMPLETE 1 KÝ TỰ
                                if (keyword.length() >= 1) {
                                    b.should(s1 -> s1.match(m -> m
                                            .field("title.autocomplete")
                                            .query(keyword)
                                    ));
                                }

                                // 🔥 FUZZY (SAI ~2–3 KÝ TỰ CẢM GIÁC)
                                if (keyword.length() >= 2) {
                                    b.should(s2 -> s2.match(m -> m
                                            .field("title")
                                            .query(keyword)
                                            .fuzziness("AUTO")       // max = 2
                                            .prefixLength(0)         // cho sai từ đầu
                                            .maxExpansions(100)      // mở rộng match
                                    ));
                                }

                                return b;
                            })),
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
