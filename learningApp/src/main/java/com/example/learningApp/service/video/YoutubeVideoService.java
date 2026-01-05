package com.example.learningApp.service.video;

import com.amazonaws.services.s3.AmazonS3;
import com.example.learningApp.dto.cache.VocabCache;
import com.example.learningApp.dto.response.video.YoutubeVideoResponse;
import com.example.learningApp.dto.response.video.YoutubeVideoSummaryResponse;
import com.example.learningApp.dto.request.video.YoutubeTranscriptRequest;
import com.example.learningApp.dto.request.video.YoutubeTranscriptRequest.TranscriptItem;
import com.example.learningApp.entity.User;
import com.example.learningApp.entity.Vocab;
import com.example.learningApp.entity.YoutubeTranscript;
import com.example.learningApp.entity.YoutubeVideo;
import com.example.learningApp.mapper.VocabMapper;
import com.example.learningApp.mapper.YoutubeVideoMapper;
import com.example.learningApp.repository.UserRepository;
import com.example.learningApp.repository.VocabRepository;
import com.example.learningApp.repository.YoutubeVideoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    private final YoutubeVideoRepository youtubeVideoRepository;
    private final YoutubeVideoMapper youtubeVideoMapper;
    private final YoutubeVideoInfoService youtubeVideoInfoService;
    private final VocabRepository vocabRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;


    @Transactional(readOnly = true)
    public List<YoutubeVideoSummaryResponse> getMySavedVideos() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String userId = authentication.getName();

        return youtubeVideoRepository
                .findSavedVideosByUserId(userId)
                .stream()
                .map(youtubeVideoMapper::toYoutubeVideoSummaryResponse)
                .toList();
    }



    public void saveVideoForUser(String videoId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        YoutubeVideo video = youtubeVideoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        // tránh save trùng
        if (user.getSavedVideos().contains(video)) {
            return;
        }

        user.getSavedVideos().add(video);

        // không bắt buộc nhưng nên có cho consistency
        video.getUsers().add(user);

        userRepository.save(user);
    }

    public void removeSavedVideo( String videoId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        YoutubeVideo video = youtubeVideoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        user.getSavedVideos().remove(video);
        video.getUsers().remove(user);

        userRepository.save(user);
    }

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

        File audioFile = downloadAudio(youtubeUrl);
        try {
            String s3Key = "audio_" + System.currentTimeMillis() + ".mp3";
            String s3Uri = uploadToS3(audioFile, s3Key);

            String jobName = "yt-transcribe-" + System.currentTimeMillis();
            createTranscriptionJob(jobName, s3Uri, languageCode);

            String transcriptJson = getTranscriptionResult(jobName);

            // Parse trực tiếp sang entity
            List<YoutubeTranscript> transcriptList = parseTranscriptionJson(transcriptJson, video);

            // Xóa transcript cũ nếu có
            video.getYoutubeTranscripts().clear();
            video.getYoutubeTranscripts().addAll(transcriptList);
            video.setUpdatedAt(Instant.now());

            youtubeVideoRepository.save(video);
            deleteFromS3(s3Key);

        } finally {
            if (audioFile.exists()) audioFile.delete();
        }

        return youtubeVideoMapper.toYoutubeVideoResponse(video);
    }


    // Download audio bằng yt-dlp + ffmpeg
    private File downloadAudio(String youtubeUrl) throws IOException, InterruptedException {
        String fileName = "audio_" + System.currentTimeMillis() + ".mp3";

        // 1. Ưu tiên biến môi trường
        String ytDlpPath = System.getenv("YT_DLP_PATH");
        String ffmpegPath = System.getenv("FFMPEG_PATH");

        // 2. Fallback: dùng exe trong thư mục tool của project
        if (ytDlpPath == null || ytDlpPath.isBlank()) {
            ytDlpPath = Paths.get("tool", "yt-dlp.exe").toAbsolutePath().toString();
        }

        if (ffmpegPath == null || ffmpegPath.isBlank()) {
            ffmpegPath = Paths.get("tool", "ffmpeg.exe").toAbsolutePath().toString();
        }

        // 3. Check tồn tại
        if (!Files.exists(Paths.get(ytDlpPath))) {
            throw new RuntimeException("Không tìm thấy yt-dlp.exe tại: " + ytDlpPath);
        }
        if (!Files.exists(Paths.get(ffmpegPath))) {
            throw new RuntimeException("Không tìm thấy ffmpeg.exe tại: " + ffmpegPath);
        }

        ProcessBuilder pb = new ProcessBuilder(
                ytDlpPath,
                "-x", "--audio-format", "mp3",
                "--ffmpeg-location", ffmpegPath,
                "-o", fileName,
                youtubeUrl
        );

        pb.inheritIO();
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("yt-dlp failed, exitCode=" + exitCode);
        }

        File audioFile = new File(fileName);
        if (!audioFile.exists()) {
            throw new RuntimeException("Audio file not created: " + fileName);
        }

        return audioFile;
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

    public void deleteFromS3(String key) {
        S3Client s3 = S3Client.builder()
                .region(Region.of(awsRegion))
                .build();

        s3.deleteObject(builder -> builder
                .bucket(s3Bucket)
                .key(key)
        );

        log.info("✅ Deleted audio from S3: {}", key);
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


    public static List<YoutubeTranscript> parseTranscriptionJson(String transcriptJson, YoutubeVideo video) {
        List<YoutubeTranscript> transcripts = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(transcriptJson);
            JsonNode items = root.path("results").path("items");

            StringBuilder sentence = new StringBuilder();
            double sentenceStart = -1;
            double sentenceEnd = -1;

            for (JsonNode item : items) {
                if (!item.has("start_time") || !item.has("alternatives")) continue;

                double start = item.path("start_time").asDouble();
                double end = item.path("end_time").asDouble();
                String word = item.path("alternatives").get(0).path("content").asText();

                if (sentenceStart < 0) sentenceStart = start;
                sentenceEnd = end;

                sentence.append(word).append(" ");

                // Nếu gặp dấu câu hoặc khoảng cách > 0.5s coi là kết thúc câu
                if (word.matches(".*[.!?]") || end - start > 0.5) {
                    YoutubeTranscript t = YoutubeTranscript.builder()
                            .video(video)
                            .startOffset((int)(sentenceStart * 1000))
                            .endOffset((int)(sentenceEnd * 1000))
                            .text(sentence.toString().trim())
                            .createdAt(LocalDateTime.now())
                            .build();
                    transcripts.add(t);

                    sentence = new StringBuilder();
                    sentenceStart = -1;
                }
            }

            // Nếu còn câu chưa kết thúc
            if (sentence.length() > 0) {
                YoutubeTranscript t = YoutubeTranscript.builder()
                        .video(video)
                        .startOffset((int)(sentenceStart * 1000))
                        .endOffset((int)(sentenceEnd * 1000))
                        .text(sentence.toString().trim())
                        .createdAt(LocalDateTime.now())
                        .build();
                transcripts.add(t);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AWS Transcribe JSON", e);
        }

        return transcripts;
    }

}