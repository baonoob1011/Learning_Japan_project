package com.example.learningApp.service.ai;

import com.example.learningApp.dto.request.exam.SubmitExamRequest;
import com.example.learningApp.dto.response.exam.SubmitExamResponse;
import com.example.learningApp.entity.ExamAnswer;
import com.example.learningApp.entity.ExamParticipant;
import com.example.learningApp.entity.Question;
import com.example.learningApp.repository.ExamAnswerRepository;
import com.example.learningApp.repository.ExamParticipantRepository;
import com.example.learningApp.repository.QuestionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ResultAIReviewService {

    private final BedrockRuntimeClient bedrockClient;
    private final String inferenceId;
    private final AIReviewToggleService toggleService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final AIConversationStore conversationStore;
    private final ExamAnswerRepository examAnswerRepository;
    private final QuestionRepository questionRepo;

    public ResultAIReviewService(
            @Value("${bedrock.region}") String region,
            @Value("${bedrock.access-key}") String accessKey,
            @Value("${bedrock.secret-key}") String secretKey,
            @Value("${bedrock.inference-id}") String inferenceId,
            AIReviewToggleService toggleService,
            AIConversationStore conversationStore,
            ExamAnswerRepository examAnswerRepository,
            QuestionRepository questionRepo
    ) {
        log.info("🧩 Initializing Bedrock client with region={}, inferenceId={}", region, inferenceId);

        var builder = BedrockRuntimeClient.builder().region(Region.of(region));

        if (accessKey != null && !accessKey.isEmpty() && secretKey != null && !secretKey.isEmpty()) {
            builder.credentialsProvider(
                    StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey))
            );
            log.info("✅ Using static AWS credentials");
        } else {
            log.warn("⚠️ Using default AWS credentials provider chain");
        }

        this.toggleService = toggleService;
        this.bedrockClient = builder.build();
        this.inferenceId = inferenceId;
        this.conversationStore = conversationStore;
        this.examAnswerRepository = examAnswerRepository;
        this.questionRepo = questionRepo;
    }
    /**
     * AI review bài thi dựa trên tất cả câu trả lời
     */
    public String reviewExamWithAI(String participantId) throws Exception {
        if (!toggleService.isEnabled()) return "AI Review is currently disabled.";

        List<ExamAnswer> answers = examAnswerRepository.findByParticipant_Id(participantId);
        if (answers.isEmpty()) return "Không có câu trả lời để AI review.";

        // Build dữ liệu bài làm
        List<Map<String, Object>> answerData = new ArrayList<>();
        for (ExamAnswer a : answers) {
            Question q = questionRepo.findById(a.getQuestionId()).orElse(null);
            if (q == null) continue;

            answerData.add(Map.of(
                    "question", q.getQuestionText(),
                    "type", q.getQuestionType(),
                    "correctAnswer", q.getAnswer(),
                    "studentAnswer", a.getAnswer(),
                    "isCorrect", a.getIsCorrect()
            ));
        }

        // 🔹 Prompt mẫu
        String prompt = """
            Bạn là giáo viên/chuyên về tiếng nhật gia chấm thi.
            Dưới đây là dữ liệu bài làm thi tiếng nhật của thí sinh (câu hỏi, đáp án đúng, đáp án thí sinh, loại câu hỏi):

            %s

            Hãy thực hiện các bước:
            1. Phân tích từng câu: đúng/sai, gợi ý cải thiện nếu sai.
            2. Tổng hợp kết quả bài làm, nêu số câu đúng, số câu sai.
            3. Đưa ra nhận xét tổng quan về bài thi (1-2 câu).

            Trả lời ngắn gọn, dễ hiểu, bằng tiếng Việt.
            """.formatted(mapper.writeValueAsString(answerData));

        String bodyJson = mapper.writeValueAsString(Map.of(
                "messages", List.of(Map.of("role", "user", "content", List.of(Map.of("type", "text", "text", prompt)))),
                "max_tokens", 800,
                "temperature", 0.3,
                "anthropic_version", "bedrock-2023-05-31"
        ));

        InvokeModelRequest request = InvokeModelRequest.builder()
                .modelId(inferenceId)
                .body(SdkBytes.fromUtf8String(bodyJson))
                .build();

        InvokeModelResponse response = bedrockClient.invokeModel(request);
        JsonNode root = mapper.readTree(response.body().asUtf8String());

        JsonNode contentNode = root.path("content");
        String reviewResult = contentNode.isArray() && contentNode.size() > 0
                ? contentNode.get(0).path("text").asText()
                : "Không nhận được phản hồi từ AI.";

        // Lưu review vào conversationStore nếu muốn
        conversationStore.addMessage(participantId.toString(), "assistant", reviewResult);

        return reviewResult;
    }


}
