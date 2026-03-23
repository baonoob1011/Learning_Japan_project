package com.example.learningApp.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class KanjiStrokeAiService {

    @Value("${gemini.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    public List<String> generateSvgStrokes(String character) {

        String prompt = """
        Generate SVG path stroke list for Kanji: %s

        Return ONLY a JSON array of SVG path strings.

        Example:
        [
          "M30,14 L30,98",
          "M30,14 L79,14 L79,98"
        ]
        """.formatted(character);

        String text = callGemini(prompt);

        try {
            return objectMapper.readValue(text, List.class);
        } catch (Exception e) {
            log.error("Failed to parse SVG JSON: {}", text, e);
            throw new RuntimeException("Invalid SVG JSON: " + text);
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
                )
        );

        try {
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
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("Gemini API error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to call Gemini API: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Unexpected error calling Gemini API", e);
            throw new RuntimeException("Unexpected error calling Gemini API", e);
        }
    }
}
