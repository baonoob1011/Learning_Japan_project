package com.example.learningApp.service.video;

import com.example.learningApp.dto.request.video.YoutubeTranscriptRequest;
import com.example.learningApp.dto.response.video.YoutubeTranscriptResponse;
import com.example.learningApp.entity.YoutubeTranscript;
import com.example.learningApp.entity.YoutubeVideo;
import com.example.learningApp.mapper.YoutubeTranscriptMapper;
import com.example.learningApp.repository.YoutubeTranscriptRepository;
import com.example.learningApp.repository.YoutubeVideoRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TranscriptService {

    YoutubeVideoRepository youtubeVideoRepository;

    public YoutubeTranscriptResponse getTranscriptsByVideoId(String videoId) {
        // Lấy video
        YoutubeVideo video = youtubeVideoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found with id: " + videoId));

        // Sắp xếp transcript theo startOffset
        List<YoutubeTranscriptResponse.transcriptsDTO> transcriptDTOs = video.getYoutubeTranscripts().stream()
                .sorted((a, b) -> Integer.compare(a.getStartOffset(), b.getStartOffset()))
                .map(t -> YoutubeTranscriptResponse.transcriptsDTO.builder()
                        .id(t.getId())
                        .text(t.getText())
                        .startOffset(t.getStartOffset())
                        .endOffset(t.getEndOffset())
                        .createdAt(t.getCreatedAt())
                        .build()
                )
                .collect(Collectors.toList());

        // Build response
        return YoutubeTranscriptResponse.builder()
                .id(video.getId())
                .title(video.getTitle())
                .videoId(video.getId())
                .urlVideo(video.getUrlVideo()) // sửa thành getUrl()
                .transcriptsDTOS(transcriptDTOs)
                .build();
    }


// Nếu cần hiển thị UI, FE mới gọi formatTime(ms) chuyển sang mm:ss

}
