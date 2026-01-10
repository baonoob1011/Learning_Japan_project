package com.example.learningApp.service.transcrip;

import com.example.learningApp.dto.event.YoutubeTranscribeMessage;
import com.example.learningApp.entity.YoutubeTranscript;
import com.example.learningApp.entity.YoutubeVideo;
import com.example.learningApp.entity.YoutubeVideoDocument;
import com.example.learningApp.enums.VideoStatus;
import com.example.learningApp.repository.YoutubeVideoRepository;
import com.example.learningApp.repository.YoutubeVideoSearchRepository;
import com.example.learningApp.service.video.YoutubeVideoInfoService;
import com.example.learningApp.service.video.YoutubeVideoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class YoutubeTranscribeConsumer {
    private final YoutubeVideoSearchRepository youtubeVideoSearchRepository;
    private final YoutubeVideoRepository youtubeVideoRepository;
    private final YoutubeVideoInfoService youtubeVideoInfoService;
    private final YoutubeVideoService youtubeVideoService;
    private final TranscriptService transcriptService;

    @KafkaListener(
            topics = "youtube-transcribe",
            concurrency = "3"
    )
    @Transactional
    public void consume(YoutubeTranscribeMessage msg)
            throws IOException, InterruptedException {

        String videoId = msg.getVideoId();

        YoutubeVideo video = youtubeVideoRepository
                .findById(videoId)
                .orElseGet(() -> {
                    try {
                        return youtubeVideoInfoService
                                .fetchAndSaveVideoInfo(msg.getUrl(), videoId);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        // 🚫 ĐÃ xử lý hoặc đang xử lý → skip
        if (video.getVideoStatus() == VideoStatus.PROCESSING ||
                video.getVideoStatus() == VideoStatus.DONE) {
            log.info("Skip video {}", videoId);
            return;
        }

        video.setVideoStatus(VideoStatus.PROCESSING);
        video.setLevel(msg.getLevel());
        video.setVideoTag(msg.getVideoTag());
        youtubeVideoRepository.save(video);

        File audioFile = youtubeVideoService.downloadAudio(msg.getUrl());
        String jobName = "yt-transcribe-" + System.currentTimeMillis(); // <- khai báo ở đây

        try {
            String s3Key = "audio_" + System.currentTimeMillis() + ".mp3";
            String s3Uri = youtubeVideoService.uploadToS3(audioFile, s3Key);

            // Tạo job transcription
            youtubeVideoService.createTranscriptionJob(jobName, s3Uri, "ja-JP");

            String transcriptJson = youtubeVideoService.getTranscriptionResult(jobName);

            List<YoutubeTranscript> transcripts = YoutubeVideoService.parseTranscriptionJson(transcriptJson, video);

            video.getYoutubeTranscripts().clear();
            video.getYoutubeTranscripts().addAll(transcripts);
            video.setVideoStatus(VideoStatus.DONE);
            video.setUpdatedAt(Instant.now());

            youtubeVideoRepository.save(video);

            YoutubeVideoDocument document = YoutubeVideoDocument.builder()
                    .id(video.getId())
                    .title(video.getTitle())
                    .urlVideo(video.getUrlVideo())
                    // chuyển enum sang string
                    .videoTag(video.getVideoTag() != null ? video.getVideoTag().name() : null)
                    .level(video.getLevel() != null ? video.getLevel().name() : null)
                    .duration(video.getDuration())
                    // LocalDateTime -> ISO String
                    .createdAt(
                            video.getCreatedAt() != null
                                    ? video.getCreatedAt().toEpochMilli()
                                    : Instant.now().toEpochMilli()
                    )

                    .build();

            youtubeVideoSearchRepository.save(document);




            youtubeVideoService.deleteFromS3(s3Key);

        } catch (Exception e) {
            video.setVideoStatus(VideoStatus.FAILED);
            youtubeVideoRepository.save(video);
            throw e; // Kafka retry
        } finally {
            if (audioFile.exists()) audioFile.delete();

            try {
                transcriptService.deleteTranscriptionJob(jobName);
            } catch (Exception e) {
                log.warn("Failed to delete transcription job: {}", jobName, e);
            }
        }

    }
}
