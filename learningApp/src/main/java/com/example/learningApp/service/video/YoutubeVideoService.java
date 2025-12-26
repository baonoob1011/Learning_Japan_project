package com.example.learningApp.service.video;

import com.example.learningApp.dto.response.video.YoutubeVideoResponse;
import com.example.learningApp.dto.response.video.YoutubeVideoSummaryResponse;
import com.example.learningApp.entity.YoutubeVideo;
import com.example.learningApp.mapper.YoutubeVideoMapper;
import com.example.learningApp.repository.YoutubeVideoRepository;
import com.example.learningApp.service.cloud.S3Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YoutubeVideoService {

    @Value("${google.api-key}")
    private String apiKey;

    private final S3Service s3Service;
    private final YoutubeVideoMapper mapper;
    private final YoutubeVideoRepository youtubeVideoRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();


    public List<YoutubeVideoSummaryResponse> getAllVideos() {
        return youtubeVideoRepository.findAll()
                .stream()
                .map(video -> new YoutubeVideoSummaryResponse(
                        video.getId(),
                        video.getTitle(),
                        video.getS3Url(),
                        video.getDuration(),
                        video.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }


    /**
     * Lấy video theo ID, map sang DTO
     */
    public YoutubeVideoResponse getVideoById(String id) {
        YoutubeVideo video = youtubeVideoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found with id: " + id));
        return mapper.toYoutubeVideoResponse(video);
    }

    /**
     * Lưu metadata video YouTube lên S3 và DB
     */
    public YoutubeVideoResponse saveYoutubeVideo(String youtubeUrl) throws IOException {
        // 1. Lấy video ID
        String videoId = extractVideoId(youtubeUrl);

        // 2. Lấy metadata từ YouTube API
        Map<String, Object> metadata = fetchYoutubeMetadata(videoId);

        // 4. Lưu DB
        YoutubeVideo video = YoutubeVideo.builder()
                .id(videoId)
                .title((String) metadata.get("title"))
                .description((String) metadata.get("description"))
                .thumbnailUrl((String) metadata.get("thumbnail"))
                .channelTitle((String) metadata.get("channelTitle"))
                .duration((String) metadata.get("duration"))
                .s3Url(youtubeUrl)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return mapper.toYoutubeVideoResponse(youtubeVideoRepository.save(video)) ;
    }

    private String extractVideoId(String youtubeUrl) {
        if (youtubeUrl.contains("v=")) {
            String vPart = youtubeUrl.split("v=")[1];
            return vPart.contains("&") ? vPart.split("&")[0] : vPart;
        } else if (youtubeUrl.contains("youtu.be/")) {
            String idPart = youtubeUrl.split("youtu.be/")[1];
            return idPart.contains("?") ? idPart.split("\\?")[0] : idPart;
        }
        throw new RuntimeException("Invalid YouTube URL");
    }



    private Map<String, Object> fetchYoutubeMetadata(String videoId) throws IOException {
        // Sử dụng YouTube Data API (bạn cần thêm API key)
        String url = "https://www.googleapis.com/youtube/v3/videos?id=" + videoId +
                "&part=snippet,contentDetails&key=" + apiKey;

        java.net.URL apiUrl = new java.net.URL(url);
        try (java.io.InputStream is = apiUrl.openStream()) {
            Map<String, Object> response = objectMapper.readValue(is, Map.class);
            Map<String, Object> item = ((java.util.List<Map<String, Object>>) response.get("items")).get(0);
            Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
            Map<String, Object> contentDetails = (Map<String, Object>) item.get("contentDetails");

            return Map.of(
                    "title", snippet.get("title"),
                    "description", snippet.get("description"),
                    "thumbnail", ((Map<String, Object>) snippet.get("thumbnails")).get("high") != null ?
                            ((Map<String, Object>) ((Map<String, Object>) snippet.get("thumbnails")).get("high")).get("url") : "",
                    "channelTitle", snippet.get("channelTitle"),
                    "duration", contentDetails.get("duration")
            );
        }
    }

}
