package com.example.learningApp.service.pronunciation;

import com.example.learningApp.dto.response.pronunciation.PronunciationResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PronunciationService {

    private final AudioConverter audioConverter;
    private final S3StorageService s3StorageService;
    private final AwsTranscribeService transcribeService;
    private final JapaneseTextNormalizer textNormalizer;
    private final PronunciationScorer scorer;

    private final ConcurrentHashMap<String, PronunciationResultResponse> resultMap =
            new ConcurrentHashMap<>();

    public String submitPronunciation(MultipartFile audioFile, String expectedText) {

        String normalizedExpected = textNormalizer.normalizeTextForJapanese(expectedText);

        File convertedFile = audioConverter.convert(audioFile);

        String s3Key = s3StorageService.upload(convertedFile);

        String jobName = "pron-" + System.currentTimeMillis();

        CompletableFuture.runAsync(() -> {

            String recognizedRaw = transcribeService.transcribe(jobName, s3Key);

            if (recognizedRaw == null) return;

            String recognizedNorm = textNormalizer.normalizeForCompare(recognizedRaw);
            String expectedNorm = textNormalizer.normalizeForCompare(normalizedExpected);

            double accuracy = scorer.calculateAccuracy(expectedNorm, recognizedNorm);

            PronunciationResultResponse result =
                    scorer.buildResult(expectedText, recognizedRaw, accuracy);

            resultMap.put(jobName, result);

            s3StorageService.delete(s3Key);
        });

        if (convertedFile.exists()) convertedFile.delete();

        return jobName;
    }

    public PronunciationResultResponse getResult(String jobName) {
        return resultMap.get(jobName);
    }
}
