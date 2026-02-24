package com.example.learningApp.service.video.transcrip;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.response.video.YoutubeTranscriptResponse;
import com.example.learningApp.entity.YoutubeVideo;
import com.example.learningApp.exception.NotFoundException;
import com.example.learningApp.repository.YoutubeVideoRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.DeleteTranscriptionJobRequest;
import software.amazon.awssdk.services.transcribe.model.TranscribeException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TranscriptService {

    TranscribeClient transcribeClient;
    EntityFinder finder;

    public YoutubeTranscriptResponse getTranscriptsByVideoId(String videoId) {
        // Lấy video
        var video = finder.videoById(videoId);

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

    public void deleteTranscriptionJob(String jobName) {
        try {
            DeleteTranscriptionJobRequest request = DeleteTranscriptionJobRequest.builder()
                    .transcriptionJobName(jobName)
                    .build();

            transcribeClient.deleteTranscriptionJob(request);
            log.info("Deleted transcription job: {}", jobName);

        } catch (TranscribeException e) {
            log.warn("AWS Transcribe failed to delete job {}: {}", jobName, e.awsErrorDetails().errorMessage());
        } catch (Exception e) {
            log.warn("Unexpected error when deleting transcription job {}: {}", jobName, e.getMessage());
        }
    }

// Nếu cần hiển thị UI, FE mới gọi formatTime(ms) chuyển sang mm:ss

}
