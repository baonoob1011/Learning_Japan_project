package com.example.learningApp.service.pronunciation;

import com.example.learningApp.dto.response.pronunciation.PronunciationResultResponse;
import com.example.learningApp.utils.SimilarityUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.*;

import java.io.File;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class PronunciationService {

    @Value("${aws.s3.bucket-nam}") private String s3Bucket;
    @Value("${aws.region-nam}") private String awsRegion;
    private final S3Client s3Client;
    private final TranscribeClient transcribeClient;
    private final ConcurrentHashMap<String, PronunciationResultResponse> resultMap = new ConcurrentHashMap<>();

    // =================== SUBMIT AUDIO ===================
    public String submitPronunciation(MultipartFile audioFile, String expectedText) {

        String normalizedText = normalizeTextForJapanese(expectedText);

        File file = convertToFile(audioFile);

        String s3Key = "pronunciation/practice_" + System.currentTimeMillis() + ".wav";
        uploadToS3(file, s3Key);

        String jobName = "pron-" + System.currentTimeMillis();
        CompletableFuture.runAsync(() -> startTranscribeJob(jobName, s3Key, normalizedText));

        if (file.exists()) file.delete();

        return jobName;
    }

    public PronunciationResultResponse getResult(String jobName) {
        return resultMap.get(jobName);
    }

    // =================== TRANSCRIBE ===================
    private void startTranscribeJob(String jobName, String s3Key, String expectedText) {
        try {

            transcribeClient.startTranscriptionJob(
                    StartTranscriptionJobRequest.builder()
                            .transcriptionJobName(jobName)
                            .languageCode("ja-JP")
                            .media(Media.builder()
                                    .mediaFileUri("s3://" + s3Bucket + "/" + s3Key)
                                    .build())
                            .mediaFormat(MediaFormat.WAV)
                            .build()
            );

            TranscriptionJob job;

            while (true) {

                job = transcribeClient.getTranscriptionJob(
                        GetTranscriptionJobRequest.builder()
                                .transcriptionJobName(jobName)
                                .build()
                ).transcriptionJob();

                if (job.transcriptionJobStatus() == TranscriptionJobStatus.COMPLETED) {

                    String recognizedRaw = fetchTranscript(job.transcript().transcriptFileUri());

                    String recognizedNormalized = normalizeForCompare(recognizedRaw);
                    String expectedNormalized = normalizeForCompare(expectedText);

                    double accuracy = SimilarityUtil.similarityPercent(expectedNormalized, recognizedNormalized);

                    PronunciationResultResponse result =
                            PronunciationResultResponse.builder()
                                    .expectedText(expectedText)
                                    .recognizedText(recognizedRaw)
                                    .accuracy(accuracy)
                                    .feedback(accuracy >= 80 ? "Phát âm tốt 👍"
                                            : accuracy >= 50 ? "Tạm ổn, cần luyện thêm ⚠️"
                                            : "Phát âm chưa đúng ❌")
                                    .build();

                    resultMap.put(jobName, result);

                    deleteFromS3(s3Key);
                    break;
                }

                if (job.transcriptionJobStatus() == TranscriptionJobStatus.FAILED) {
                    log.error("❌ Transcribe failed: {}", job.failureReason());
                    deleteFromS3(s3Key);
                    break;
                }

                Thread.sleep(2000);
            }

        } catch (Exception e) {
            log.error("❌ Transcribe job error", e);
            deleteFromS3(s3Key);
        }
    }

    // =================== FILE UTILS ===================
    private File convertToFile(MultipartFile multipartFile) {
        try {
            String originalName = multipartFile.getOriginalFilename();
            String ext = originalName != null && originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : ".tmp";

            File raw = File.createTempFile("pron_raw_", ext);
            File normalized = File.createTempFile("pron_", ".wav");
            multipartFile.transferTo(raw);

            // 🔥 Lấy đường dẫn tuyệt đối tới tool/ffmpeg.exe
            String ffmpegPath = new File("tool/ffmpeg.exe").getAbsolutePath();

            ProcessBuilder pb = new ProcessBuilder(
                    ffmpegPath,
                    "-y",
                    "-i", raw.getAbsolutePath(),
                    "-ac", "1",
                    "-ar", "16000",
                    "-vn",
                    "-acodec", "pcm_s16le",
                    normalized.getAbsolutePath()
            );

            pb.redirectErrorStream(true);

            int exit = pb.start().waitFor();
            if (exit != 0) throw new RuntimeException("ffmpeg convert failed");

            raw.delete();
            return normalized;

        } catch (Exception e) {
            throw new RuntimeException("Convert MultipartFile failed", e);
        }
    }

    private void uploadToS3(File file, String key) {
        try {

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(s3Bucket)
                            .key(key)
                            .contentType("audio/wav")
                            .build(),
                    file.toPath()
            );

            log.info("✅ Uploaded audio to S3: {}", key);

        } catch (Exception e) {
            log.error("❌ S3 Upload error", e);
            throw e;
        }
    }

    private void deleteFromS3(String key) {
        try {

            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(s3Bucket)
                            .key(key)
                            .build()
            );

            log.info("🗑️ Deleted audio from S3: {}", key);

        } catch (Exception e) {
            log.error("❌ Failed to delete S3 file: {}", key, e);
        }
    }
    private String fetchTranscript(String transcriptUrl) throws Exception {
        HttpResponse<String> response = java.net.http.HttpClient.newHttpClient()
                .send(HttpRequest.newBuilder().uri(URI.create(transcriptUrl)).GET().build(),
                        HttpResponse.BodyHandlers.ofString());

        log.info("📜 Raw Transcribe JSON: {}", response.body());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode items = mapper.readTree(response.body()).path("results").path("items");

        StringBuilder recognizedBuilder = new StringBuilder();

        for (JsonNode item : items) {
            if (!"pronunciation".equals(item.path("type").asText())) continue;

            String token = item.path("alternatives").get(0).path("content").asText();
            String confidence = item.path("alternatives").get(0).path("confidence").asText();

            // ⚠️ Do NOT convert digits here, keep raw
            recognizedBuilder.append(token);
            log.info("🔹 Token recognized (raw): '{}' (confidence: {})", token, confidence);
        }

        return recognizedBuilder.toString();
    }

    // =================== NORMALIZE TEXT ===================
    private String normalizeTextForJapanese(String text) {
        return text.replace("0", "零")
                .replace("1", "一")
                .replace("2", "二")
                .replace("3", "三")
                .replace("4", "四")
                .replace("5", "五")
                .replace("6", "六")
                .replace("7", "七")
                .replace("8", "八")
                .replace("9", "九");
    }

    private String normalizeForCompare(String text) {
        if (text == null) return "";
        // Remove punctuation
        text = text.replaceAll("[、。！？,.?!]", "")
                .replaceAll("\\s+", "");

        // Map phonetic/Arabic numbers to Kanji
        text = text.replace("に","二")
                .replace("2","二")
                .replace("さん","三")
                .replace("3","三")
                .replace("し","四")
                .replace("4","四")
                .replace("ご","五")
                .replace("5","五")
                .replace("ろく","六")
                .replace("6","六")
                .replace("なな","七")
                .replace("7","七")
                .replace("はち","八")
                .replace("8","八")
                .replace("きゅう","九")
                .replace("9","九")
                .replace("0","零")
                .replace("ぜろ","零");

        return text;
    }

}
