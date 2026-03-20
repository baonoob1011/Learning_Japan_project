package com.example.learningApp.service.video;

import com.example.learningApp.entity.YoutubeVideo;
import com.example.learningApp.repository.YoutubeVideoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class YoutubeVideoInfoService {

    @Value("${google.api-key}")
    private String youtubeApiKey;

    private final YoutubeVideoRepository youtubeVideoRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String extractVideoId(String url) {
        if (url.contains("v=")) {
            return url.substring(url.indexOf("v=") + 2);
        }
        if (url.contains("youtu.be/")) {
            return url.substring(url.lastIndexOf("/") + 1);
        }
        throw new RuntimeException("Invalid YouTube URL");
    }

    public YoutubeVideo fetchAndSaveVideoInfo(String youtubeUrl,String videoId) throws IOException, InterruptedException {

        // 1️⃣ Gọi YouTube API
        String apiUrl = "https://www.googleapis.com/youtube/v3/videos" +
                "?part=snippet,contentDetails" +
                "&id=" + videoId +
                "&key=" + youtubeApiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode items = root.path("items");

        if (items.isEmpty()) {
            throw new RuntimeException("Video not found on YouTube: " + videoId);
        }

        JsonNode videoJson = items.get(0);
        JsonNode snippet = videoJson.path("snippet");
        JsonNode contentDetails = videoJson.path("contentDetails");

        // 2️⃣ Lấy video từ DB hoặc tạo mới
        YoutubeVideo video = youtubeVideoRepository.findById(videoId)
                .orElseGet(() -> YoutubeVideo.builder()
                        .id(videoId)
                        .createdAt(Instant.now())
                        .youtubeTranscripts(new java.util.ArrayList<>())
                        .build()
                );

        // 3️⃣ Map dữ liệu từ API
        video.setTitle(snippet.path("title").asText());
        video.setDescription(snippet.path("description").asText());
        video.setChannelTitle(snippet.path("channelTitle").asText());
        video.setThumbnailUrl(snippet.path("thumbnails").path("high").path("url").asText());
        video.setDuration(contentDetails.path("duration").asText());
        video.setPublishedAt(Instant.parse(snippet.path("publishedAt").asText()));
        video.setUrlVideo(youtubeUrl);
        video.setUpdatedAt(Instant.now());

        // 4️⃣ Lưu xuống DB
        return youtubeVideoRepository.save(video);
    }

    // Hàm tách videoId từ UR
}

