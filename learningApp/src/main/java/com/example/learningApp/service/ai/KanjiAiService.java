package com.example.learningApp.service.ai;

import com.example.learningApp.dto.response.kanji.KanjiAiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KanjiAiService {

    @Value("${gemini.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    public KanjiAiResponse generateKanjiData(String character) {

        String prompt = """
        Generate Japanese Kanji information in JSON format.

        Character: %s

        Return ONLY valid JSON in this structure:

        {
          "meaning": "...",
          "onyomi": "...",
          "kunyomi": "...",
          "strokeData": [
            [ {"x":50,"y":10}, {"x":50,"y":90} ],
            [ {"x":20,"y":40}, {"x":50,"y":70} ]
          ]
        }

        Return JSON only.
        """.formatted(character);

        String text = callGemini(prompt);

        try {
            return objectMapper.readValue(text, KanjiAiResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Invalid Kanji AI JSON: " + text);
        }
    }

    private String callGemini(String prompt) {

        String url =
                "https://generativelanguage.googleapis.com/v1beta/models/"
                        + "gemini-3-flash-preview:generateContent?key="
                        + apiKey;

       HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(Map.of("text", prompt))
                        )
                ),
                "generationConfig", Map.of(
                        "responseMimeType", "application/json"
                )
        );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(url, request, Map.class);

        List<Map<String, Object>> candidates =
                (List<Map<String, Object>>) response.getBody().get("candidates");

        Map<String, Object> content =
                (Map<String, Object>) candidates.get(0).get("content");

        List<Map<String, Object>> parts =
                (List<Map<String, Object>>) content.get("parts");

        return parts.get(0).get("text").toString().trim();
    }
}
