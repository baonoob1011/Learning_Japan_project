package com.example.learningApp.service.vocab;

import com.example.learningApp.entity.Vocab;
import com.example.learningApp.repository.VocabRepository;
import com.example.learningApp.service.translate.interfaces.AudioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class VocabEnrichmentService {

    private final VocabRepository vocabRepository;
    private final AudioService audioService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gemini.api-key}")
    private String apiKey;

    public void enrichVocab(String vocabId) {
        Vocab vocab = vocabRepository.findById(vocabId).orElse(null);
        if (vocab == null)
            return;

        log.info("Starting enrichment for vocab: {}", vocab.getSurface());

        // 1. Generate Example (Gemini) - Parallel
        CompletableFuture<String> exampleFuture = CompletableFuture
                .supplyAsync(() -> generateExampleViaGemini(vocab.getSurface(), vocab.getTranslated()));

        // 2. Generate Audio (AWS Polly) - Parallel
        CompletableFuture<String> audioFuture = CompletableFuture.supplyAsync(() -> {
            if (vocab.getAudioUrl() != null && !vocab.getAudioUrl().isEmpty()) {
                return vocab.getAudioUrl();
            }
            return audioService.generateAudio(vocab.getSurface());
        });

        CompletableFuture.allOf(exampleFuture, audioFuture).join();

        try {
            String example = exampleFuture.get();
            String audioUrl = audioFuture.get();

            vocab.setExample(example);
            vocab.setAudioUrl(audioUrl);
            vocabRepository.save(vocab);

            log.info("Enrichment completed for vocab: {}", vocab.getSurface());
        } catch (Exception e) {
            log.error("Failed to enrich vocab: {}", vocab.getSurface(), e);
        }
    }

    private String generateExampleViaGemini(String word, String meaning) {
        try {
            String prompt = String.format(
                    "Hãy tạo 1 câu ví dụ tiếng Nhật ngắn gọn, thông dụng cho từ vựng: '%s' (nghĩa: %s). " +
                            "Kết quả trả về CHỈ bao gồm câu ví dụ tiếng Nhật và phần dịch tiếng Việt bên dưới, không giải thích gì thêm. "
                            +
                            "Định dạng: [Câu tiếng Nhật] \n [Dịch tiếng Việt]",
                    word, meaning);

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key="
                    + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "contents", List.of(
                            Map.of("role", "user", "parts", List.of(Map.of("text", prompt)))));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            var response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                var candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
                @SuppressWarnings("unchecked")
                var content = (Map<String, Object>) candidates.get(0).get("content");
                @SuppressWarnings("unchecked")
                var parts = (List<Map<String, Object>>) content.get("parts");
                return parts.get(0).get("text").toString().trim();
            }
        } catch (Exception e) {
            log.error("Gemini example generation failed for word: {}", word, e);
        }
        return null;
    }
}
