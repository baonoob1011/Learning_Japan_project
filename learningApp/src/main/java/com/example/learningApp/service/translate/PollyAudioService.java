package com.example.learningApp.service.translate;

import com.example.learningApp.service.cloud.S3Service;
import com.example.learningApp.service.translate.interfaces.AudioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.OutputFormat;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;

@Service
@RequiredArgsConstructor
public class PollyAudioService implements AudioService {

    private final PollyClient pollyClient;
    private final S3Service s3Service;

    @Override
    public String generateAudio(String text) {
        byte[] audioBytes;
        try (var res = pollyClient.synthesizeSpeech(
                SynthesizeSpeechRequest.builder()
                        .text(text)
                        .voiceId("Mizuki")
                        .outputFormat(OutputFormat.MP3)
                        .build()
        )) {
            audioBytes = res.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return s3Service.uploadBytes(audioBytes, "tts", ".mp3");
    }
}


