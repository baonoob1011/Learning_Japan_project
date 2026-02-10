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
        final int size = q.length() == 1 ? 3 : q.length() == 2 ? 5 : 10;

        try {
            var response = elasticsearchClient.search(s -> s
                            .index("youtube_videos")
                            .size(size)
                            .query(query -> query.bool(b -> {

                                /* =========================
                                   🔍 1 KÝ TỰ → PREFIX (RẤT CHẶT)
                                ========================== */
                                if (q.length() == 1) {
                                    b.must(m -> m.prefix(p -> p
                                            .field("title")
                                            .value(q)
                                            .caseInsensitive(true)
                                    ));
                                    return b;
                                }

                                /* =========================
                                   🔍 2 KÝ TỰ → AUTOCOMPLETE
                                ========================== */
                                b.must(m -> m.match(mt -> mt
                                        .field("title.autocomplete")
                                        .query(q)
                                ));

                                /* =========================
                                   🔥 ≥ 3 → FUZZY GỢI Ý
                                ========================== */
                                if (q.length() >= 3) {
                                    b.should(s2 -> s2.match(m -> m
                                            .field("title")
                                            .query(q)
                                            .fuzziness("AUTO")
                                            .prefixLength(1)
                                            .maxExpansions(20)
                                    ));
                                    b.minimumShouldMatch("0");
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
