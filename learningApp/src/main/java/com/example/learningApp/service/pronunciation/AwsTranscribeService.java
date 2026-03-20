package com.example.learningApp.service.pronunciation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.*;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwsTranscribeService {

    private final TranscribeClient transcribeClient;

    @Value("${aws.s3.bucket-nam}")
    private String bucket;

    public String transcribe(String jobName, String s3Key) {

        try {

            transcribeClient.startTranscriptionJob(
                    StartTranscriptionJobRequest.builder()
                            .transcriptionJobName(jobName)
                            .languageCode("ja-JP")
                            .media(Media.builder()
                                    .mediaFileUri("s3://" + bucket + "/" + s3Key)
                                    .build())
                            .mediaFormat(MediaFormat.WAV)
                            .build()
            );

            while (true) {

                TranscriptionJob job = transcribeClient.getTranscriptionJob(
                        GetTranscriptionJobRequest.builder()
                                .transcriptionJobName(jobName)
                                .build()
                ).transcriptionJob();

                if (job.transcriptionJobStatus() == TranscriptionJobStatus.COMPLETED) {
                    return fetchTranscript(job.transcript().transcriptFileUri());
                }

                if (job.transcriptionJobStatus() == TranscriptionJobStatus.FAILED) {
                    log.error("❌ Transcribe failed: {}", job.failureReason());
                    return null;
                }

                Thread.sleep(2000);
            }

        } catch (Exception e) {
            log.error("❌ Transcribe error", e);
            return null;
        }
    }

    private String fetchTranscript(String transcriptUrl) throws Exception {

        HttpResponse<String> response = java.net.http.HttpClient.newHttpClient()
                .send(HttpRequest.newBuilder().uri(URI.create(transcriptUrl)).GET().build(),
                        HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode items = mapper.readTree(response.body()).path("results").path("items");

        StringBuilder recognized = new StringBuilder();

        for (JsonNode item : items) {
            if (!"pronunciation".equals(item.path("type").asText())) continue;

            String token = item.path("alternatives").get(0).path("content").asText();
            recognized.append(token);
        }

        return recognized.toString();
    }
}
