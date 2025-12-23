package com.example.learningApp.service.ai;

import com.example.learningApp.entity.Question;
import com.example.learningApp.repository.QuestionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class QuestionChatAIGeminiService {
    private final QuestionRepository questionRepo;
    private final ObjectMapper mapper = new ObjectMapper();
    private final WebClient webClient;
    private final String apiKey;

    public QuestionChatAIGeminiService(
            QuestionRepository questionRepo,
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.endpoint}") String endpoint
    ) {
        this.questionRepo = questionRepo;
        this.apiKey = apiKey;

        this.webClient = WebClient.builder()
                .baseUrl(endpoint)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();

        log.info("✅ Gemini client initialized (outside AWS)");
    }

    public String chatWithQuestion(String questionId, String userMessage) throws Exception {

        Question q = questionRepo.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        List<String> options = (q.getOptions() == null || q.getOptions().isBlank())
                ? List.of()
                : mapper.readValue(q.getOptions(), List.class);

        String prompt = buildPrompt(q, options, userMessage);

        Map<String, Object> requestBody = Map.of(
                "inputText", prompt,
                "maxOutputTokens", 120,
                "temperature", 0.1
        );

        JsonNode response = webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        return response.path("outputText").asText();
    }

    private String buildPrompt(Question q, List<String> options, String userMessage) {
        return """
        Bạn là giáo viên tiếng Nhật JLPT N5.

        Câu hỏi:
        %s

        Các lựa chọn:
        %s

        Học viên hỏi:
        %s

        Yêu cầu:
        - Giải thích ngắn gọn, dễ hiểu
        - Không tiết lộ đáp án nếu không được hỏi
        - Trả lời bằng tiếng Việt
        """.formatted(
                q.getQuestionText(),
                options.isEmpty() ? "(Không có)" : String.join(", ", options),
                (userMessage == null || userMessage.isBlank())
                        ? "Giải thích câu hỏi này"
                        : userMessage
        );
    }
}
