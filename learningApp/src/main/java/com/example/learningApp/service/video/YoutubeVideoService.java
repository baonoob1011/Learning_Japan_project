package com.example.learningApp.service.video;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.learningApp.component.kafka.Producer;
import com.example.learningApp.dto.cache.VocabCache;
import com.example.learningApp.dto.response.video.YoutubeVideoResponse;
import com.example.learningApp.dto.response.video.YoutubeVideoSummaryResponse;
import com.example.learningApp.entity.Vocab;
import com.example.learningApp.entity.YoutubeTranscript;
import com.example.learningApp.entity.YoutubeVideo;
import com.example.learningApp.mapper.YoutubeVideoMapper;
import com.example.learningApp.repository.VocabRepository;
import com.example.learningApp.repository.YoutubeVideoRepository;
import com.example.learningApp.service.audio.AudioService;
import com.example.learningApp.service.cloud.S3Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class YoutubeVideoService {

    @Value("${openai.api-key}")
    private String openAiApiKey;
    @Value("${aws.s3.bucket-nam}")
    private String s3Bucket;

    @Value("${aws.region-nam}")
    private String awsRegion;

    private final AmazonS3 amazonS3;


    private static final String TRANSCRIPT_READY = "transcript-ready";
    private final AudioService audioService;
    private final S3Service s3Service;
    private final YoutubeVideoRepository youtubeVideoRepository;
    private final YoutubeVideoMapper youtubeVideoMapper;
    private final YoutubeVideoInfoService youtubeVideoInfoService;
    private final VocabRepository vocabRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    // Lấy tất cả video
    public List<YoutubeVideoSummaryResponse> getAllVideos() {
        return youtubeVideoRepository.findAll()
                .stream()
                .map(video -> new YoutubeVideoSummaryResponse(
                        video.getId(),
                        video.getTitle(),
                        video.getUrlVideo(),
                        video.getDuration(),
                        video.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    // Lấy video theo ID
    public Void getVideoById(String id) {
        // Lấy video, nếu không tồn tại thì throw
        YoutubeVideo video = youtubeVideoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found with id: " + id));

        // Lấy vocab liên quan và cache từng vocab riêng lẻ
        List<Vocab> vocabList = vocabRepository.findAllByVideoId(id);
        if (!vocabList.isEmpty()) {
            for (Vocab vocab : vocabList) {
                VocabCache cache = new VocabCache();
                cache.setId(vocab.getId());
                cache.setSurface(vocab.getSurface());
                cache.setRomaji(vocab.getRomaji());
                cache.setTranslated(vocab.getTranslated());
                cache.setReading(vocab.getReading());
                cache.setTargetDefs(vocab.getTargetDefs());
                cache.setPartOfSpeech(vocab.getPartOfSpeech());
                cache.setAudioUrl(vocab.getAudioUrl());

                String redisKey = "vocabCache:" + id + ":" + vocab.getSurface().toLowerCase();
                redisTemplate.opsForValue().set(redisKey, cache, Duration.ofHours(1));
            }
        }

        return null;
    }



    // ------------------- MAIN FLOW -------------------
    public YoutubeVideoResponse saveYoutubeTranscriptAws(String youtubeUrl, String languageCode) throws IOException, InterruptedException {
        log.info("===== Start saveYoutubeTranscriptAws =====");

        String videoId = extractVideoId(youtubeUrl);
        YoutubeVideo video = youtubeVideoRepository.findById(videoId)
                .orElseGet(() -> {
                    try {
                        return youtubeVideoInfoService.fetchAndSaveVideoInfo(youtubeUrl, videoId);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });

        if (!youtubeVideoRepository.existsById(video.getId())) {
            youtubeVideoRepository.save(video);
        }

        File audioFile = audioService.downloadAudio(youtubeUrl);
        try {
            String s3Key = "audio_" + System.currentTimeMillis() + ".mp3";
            String s3Uri = uploadToS3(audioFile, s3Key);

            String jobName = "yt-transcribe-" + System.currentTimeMillis();
            createTranscriptionJob(jobName, s3Uri, languageCode);

            String transcriptJson = getTranscriptionResult(jobName);




            // Parse trực tiếp sang entity
            List<YoutubeTranscript> transcriptList =
                    parseTranscriptionJson(transcriptJson, video, audioFile);

            // Xóa transcript cũ nếu có
            video.getYoutubeTranscripts().clear();
            video.getYoutubeTranscripts().addAll(transcriptList);
            video.setUpdatedAt(Instant.now());

            youtubeVideoRepository.save(video);

        } finally {
            if (audioFile.exists()) audioFile.delete();
        }

        return youtubeVideoMapper.toYoutubeVideoResponse(video);
    }


    // Lấy videoId từ URL YouTube
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



    public String uploadToS3(File audioFile, String key) throws IOException {
        S3Client s3 = S3Client.builder().region(Region.of(awsRegion)).build();
        s3.putObject(PutObjectRequest.builder().bucket(s3Bucket).key(key).build(), audioFile.toPath());
        return "s3://" + s3Bucket + "/" + key;
    }

    // ------------------- AWS Transcribe -------------------

    public void createTranscriptionJob(String jobName, String s3Uri, String languageCode) {
        TranscribeClient client = TranscribeClient.builder()
                .region(Region.of(awsRegion))
                .build();
        StartTranscriptionJobRequest request = StartTranscriptionJobRequest.builder()
                .transcriptionJobName(jobName)
                .languageCode(languageCode)
                .media(Media.builder().mediaFileUri(s3Uri).build())
                // Bỏ outputBucketName -> AWS tự quản lý bucket
                .build();
        client.startTranscriptionJob(request);
    }

    public String getTranscriptionResult(String jobName) throws IOException, InterruptedException {
        TranscribeClient client = TranscribeClient.builder()
                .region(Region.of(awsRegion))
                .build();

        TranscriptionJob job;
        int retries = 0;
        do {
            GetTranscriptionJobResponse response = client.getTranscriptionJob(
                    GetTranscriptionJobRequest.builder()
                            .transcriptionJobName(jobName)
                            .build()
            );
            job = response.transcriptionJob();
            if (job.transcriptionJobStatus() == TranscriptionJobStatus.IN_PROGRESS) {
                Thread.sleep(Math.min(1000 * (retries + 1), 10000));
                retries++;
            }
        } while (job.transcriptionJobStatus() == TranscriptionJobStatus.IN_PROGRESS);

        if (job.transcriptionJobStatus() == TranscriptionJobStatus.FAILED) {
            throw new RuntimeException("Transcription job failed: " + job.failureReason());
        }

        // Lấy URL JSON từ Transcribe (pre-signed URL)
        String transcriptUrl = job.transcript().transcriptFileUri();

        // Fetch trực tiếp bằng HttpClient
        HttpResponse<String> httpResponse = HttpClient.newHttpClient()
                .send(HttpRequest.newBuilder().uri(URI.create(transcriptUrl)).GET().build(),
                        HttpResponse.BodyHandlers.ofString());

        String transcriptJson = httpResponse.body();
        if (!transcriptJson.startsWith("{")) {
            throw new RuntimeException("Invalid transcript JSON: " + transcriptJson);
        }

        return transcriptJson;
    }

    public List<YoutubeTranscript> parseTranscriptionJson(
            String transcriptJson,
            YoutubeVideo video,
            File sourceAudio
    ) {
        List<YoutubeTranscript> transcripts = new ArrayList<>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode items = mapper.readTree(transcriptJson)
                    .path("results")
                    .path("items");

            StringBuilder sentence = new StringBuilder();
            double sentenceStart = -1;
            double lastEndTime = -1;

            for (JsonNode item : items) {

                String type = item.path("type").asText();

                // 🔹 WORD
                if ("pronunciation".equals(type)) {
                    double start = item.path("start_time").asDouble();
                    double end = item.path("end_time").asDouble();
                    String word = item.path("alternatives").get(0).path("content").asText();

                    if (sentenceStart < 0) sentenceStart = start;
                    lastEndTime = end;

                    sentence.append(word).append(" ");

                    // 🔥 cắt nếu câu quá dài (3s)
                    if ((lastEndTime - sentenceStart) >= 3.0 && lastEndTime > sentenceStart) {
                        flushSentenceSafe(transcripts, video, sourceAudio,
                                sentence, sentenceStart, lastEndTime);
                        sentence = new StringBuilder();
                        sentenceStart = -1;
                    }
                }

                // 🔹 PUNCTUATION
                else if ("punctuation".equals(type) && sentence.length() > 0) {
                    String punc = item.path("alternatives").get(0).path("content").asText();

                    // bỏ space trước dấu câu
                    if (sentence.charAt(sentence.length() - 1) == ' ') {
                        sentence.setLength(sentence.length() - 1);
                    }
                    sentence.append(punc).append(" ");

                    if (punc.matches("[.!?]")) {
                        flushSentenceSafe(transcripts, video, sourceAudio,
                                sentence, sentenceStart, lastEndTime);
                        sentence = new StringBuilder();
                        sentenceStart = -1;
                    }
                }
            }

            // 🔥 FIX QUAN TRỌNG – CÂU CUỐI
            // 🔥 FIX QUAN TRỌNG – CÂU CUỐI
            if (sentence.length() > 0 && sentenceStart >= 0 && lastEndTime > sentenceStart) {
                flushSentenceSafe(transcripts, video, sourceAudio,
                        sentence, sentenceStart, lastEndTime);
            }


        } catch (Exception e) {
            throw new RuntimeException("Parse + cut audio failed", e);
        }

        return transcripts;
    }

    private void flushSentenceSafe(
            List<YoutubeTranscript> transcripts,
            YoutubeVideo video,
            File sourceAudio,
            StringBuilder sentence,
            double startSec,
            double endSec
    ) throws IOException, InterruptedException {

        // 🛡 guard tuyệt đối
        if (startSec < 0 || endSec <= startSec || sentence.length() == 0) {
            return;
        }

        int startMs = (int) (startSec * 1000);
        int endMs = (int) (endSec * 1000);
        endMs = Math.max(endMs, startMs + 300);

        File audio = audioService.cutAudio(
                sourceAudio,
                startMs,
                endMs,
                "sentence_" + video.getId() + "_" + startMs + "_" + System.nanoTime() + ".mp3"
        );


        String audioUrl = uploadFile(
                audio,
                "sentence/" + video.getId()
        );

        YoutubeTranscript t = YoutubeTranscript.builder()
                .video(video)
                .text(sentence.toString().trim())
                .startOffset(startMs)
                .endOffset(endMs)
                .audioUrl(audioUrl)
                .createdAt(LocalDateTime.now())
                .build();

        transcripts.add(t);
        audio.delete();
    }
    public String uploadFile(File file, String folder) throws IOException {
        String fileName = folder + "/" + UUID.randomUUID() + "_" + file.getName();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.length());

        amazonS3.putObject(
                s3Bucket,
                fileName,
                Files.newInputStream(file.toPath()),
                metadata
        );

        return buildPublicUrl(fileName);
    }
    private String buildPublicUrl(String key) {
        return "https://" + s3Bucket + ".s3."
                + amazonS3.getRegionName()
                + ".amazonaws.com/" + key;
    }
}