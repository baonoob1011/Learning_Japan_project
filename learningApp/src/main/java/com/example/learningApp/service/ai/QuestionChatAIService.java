package com.example.learningApp.service.ai;

import com.example.learningApp.entity.Question;
import com.example.learningApp.repository.QuestionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class QuestionChatAIService {

    private final BedrockRuntimeClient bedrockClient;
    private final QuestionRepository questionRepo;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String inferenceId;

    public QuestionChatAIService(
            QuestionRepository questionRepo,
            @Value("${bedrock.region}") String region,
            @Value("${bedrock.access-key}") String accessKey,
            @Value("${bedrock.secret-key}") String secretKey,
            @Value("${bedrock.inference-id}") String inferenceId
    ) {
        this.questionRepo = questionRepo;
        this.inferenceId = inferenceId;

        this.bedrockClient = BedrockRuntimeClient.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .build();

        log.info("✅ Bedrock client initialized (QuestionChatAIService)");
    }

    // ================= CHAT =================

    public String chatWithQuestion(String questionId, String userMessage) throws Exception {

        Question q = questionRepo.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        List<String> options = (q.getOptions() == null || q.getOptions().isBlank())
                ? List.of()
                : mapper.readValue(q.getOptions(), List.class);

        String prompt = buildPrompt(q, options, userMessage);

        String bodyJson = mapper.writeValueAsString(Map.of(
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", List.of(
                                        Map.of("type", "text", "text", prompt)
                                )
                        )
                ),
                "max_tokens", 120,
                "temperature", 0.1,
                "anthropic_version", "bedrock-2023-05-31"
        ));

        InvokeModelRequest request = InvokeModelRequest.builder()
                .modelId(inferenceId)
                .body(SdkBytes.fromUtf8String(bodyJson))
                .build();

        int maxRetry = 3;
        long delay = 500;

        for (int attempt = 1; attempt <= maxRetry; attempt++) {
            try {
                InvokeModelResponse response = bedrockClient.invokeModel(request);
                JsonNode root = mapper.readTree(response.body().asUtf8String());
                return root.path("content").get(0).path("text").asText();

            } catch (Exception e) {
                if (e.getMessage() != null &&
                        (e.getMessage().contains("429") || e.getMessage().contains("Too many tokens"))) {

                    log.warn("⚠️ Bedrock throttling – retry {}/{}", attempt, maxRetry);

                    if (attempt == maxRetry) break;

                    Thread.sleep(delay);
                    delay *= 2; // exponential backoff
                } else {
                    throw e;
                }
            }
        }

        return "AI đang quá tải 😅 Bạn thử lại sau 10–20 giây nhé.";
    }


    // ================= PROMPT =================

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
