package com.example.learningApp.service.pronunciation;

import com.example.learningApp.dto.response.pronunciation.PronunciationResultResponse;
import com.example.learningApp.utils.SimilarityUtil;
import org.springframework.stereotype.Component;

@Component
public class PronunciationScorer {

    public double calculateAccuracy(String expected, String actual) {
        return SimilarityUtil.similarityPercent(expected, actual);
    }

    public PronunciationResultResponse buildResult(
            String expected,
            String recognized,
            double accuracy
    ) {

        String feedback =
                accuracy >= 80 ? "Phát âm tốt 👍"
                        : accuracy >= 50 ? "Tạm ổn, cần luyện thêm ⚠️"
                        : "Phát âm chưa đúng ❌";

        return PronunciationResultResponse.builder()
                .expectedText(expected)
                .recognizedText(recognized)
                .accuracy(accuracy)
                .feedback(feedback)
                .build();
    }
}
