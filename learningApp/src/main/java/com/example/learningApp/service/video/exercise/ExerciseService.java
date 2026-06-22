package com.example.learningApp.service.video.exercise;

import com.example.learningApp.dto.request.excercise.GenerateExerciseRequest;
import com.example.learningApp.dto.response.excercise.ExerciseDetailResponse;
import com.example.learningApp.dto.response.excercise.ExerciseResponse;
import com.example.learningApp.dto.response.excercise.OptionResponse;
import com.example.learningApp.dto.response.excercise.QuestionResponse;
import com.example.learningApp.entity.*;
import com.example.learningApp.repository.*;
import com.example.learningApp.service.ai.ChatbotService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final YoutubeVideoRepository videoRepository;
    private final YoutubeTranscriptRepository transcriptRepository;
    private final ExerciseRepository exerciseRepository;
    private final QuestionVideoYoutubeRepository questionRepository;
    private final AnswerOptionRepository answerOptionRepository;
    private final ChatbotService chatbotService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Lấy exercise + questions theo videoId
     * Nếu không có -> trả về null (không throw)
     */
    @Transactional(readOnly = true)
    public ExerciseDetailResponse getExerciseWithQuestionsByVideoId(String videoId) {

        Exercise exercise = exerciseRepository
                .findFirstByVideoIdOrderByCreatedAtDesc(videoId)
                .orElse(null);

        if (exercise == null) {
            return null;
        }

        List<QuestionVideoYoutube> questions =
                questionRepository.findByExerciseId(exercise.getId());

        if (questions == null || questions.isEmpty()) {
            return ExerciseDetailResponse.builder()
                    .id(exercise.getId())
                    .videoId(exercise.getVideo().getId())
                    .title(exercise.getTitle())
                    .description(exercise.getDescription())
                    .totalQuestions(0)
                    .createdAt(exercise.getCreatedAt())
                    .questions(List.of())
                    .build();
        }

        List<QuestionResponse> questionResponses = new ArrayList<>();

        for (QuestionVideoYoutube q : questions) {
            List<AnswerOption> options =
                    answerOptionRepository.findByQuestionId(q.getId());

            List<OptionResponse> optionResponses = options == null
                    ? List.of()
                    : options.stream()
                    .map(o -> OptionResponse.builder()
                            .optionIndex(o.getOptionIndex())
                            .content(o.getContent())
                            .correct(o.isCorrect())
                            .build())
                    .toList();

            questionResponses.add(
                    QuestionResponse.builder()
                            .questionId(q.getId())
                            .transcriptText(
                                    q.getTranscript() != null
                                            ? q.getTranscript().getText()
                                            : null
                            )
                            .questionText(q.getQuestionText())
                            .questionType(q.getQuestionType())
                            .options(optionResponses)
                            .build()
            );
        }

        return ExerciseDetailResponse.builder()
                .id(exercise.getId())
                .videoId(exercise.getVideo().getId())
                .title(exercise.getTitle())
                .description(exercise.getDescription())
                .totalQuestions(exercise.getTotalQuestions())
                .createdAt(exercise.getCreatedAt())
                .questions(questionResponses)
                .build();
    }

    /**
     * Lấy exercise basic
     * Nếu không có -> trả null
     */
    @Transactional(readOnly = true)
    public ExerciseDetailResponse getExercise(String exerciseId) {
        Exercise exercise = exerciseRepository.findById(exerciseId).orElse(null);

        if (exercise == null) {
            return null;
        }

        return ExerciseDetailResponse.builder()
                .id(exercise.getId())
                .videoId(exercise.getVideo().getId())
                .title(exercise.getTitle())
                .description(exercise.getDescription())
                .totalQuestions(exercise.getTotalQuestions())
                .createdAt(exercise.getCreatedAt())
                .questions(List.of())
                .build();
    }

    /**
     * Lấy danh sách câu hỏi theo exercise
     * Nếu không có -> trả list rỗng
     */
    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestionsByExercise(String exerciseId) {
        List<QuestionVideoYoutube> questions =
                questionRepository.findByExerciseId(exerciseId);

        if (questions == null || questions.isEmpty()) {
            return List.of();
        }

        List<QuestionResponse> responses = new ArrayList<>();

        for (QuestionVideoYoutube q : questions) {
            List<AnswerOption> options =
                    answerOptionRepository.findByQuestionId(q.getId());

            List<OptionResponse> optionResponses = options == null
                    ? List.of()
                    : options.stream()
                    .map(o -> OptionResponse.builder()
                            .optionIndex(o.getOptionIndex())
                            .content(o.getContent())
                            .build())
                    .toList();

            responses.add(
                    QuestionResponse.builder()
                            .questionId(q.getId())
                            .transcriptText(
                                    q.getTranscript() != null
                                            ? q.getTranscript().getText()
                                            : null
                            )
                            .questionText(q.getQuestionText())
                            .questionType(q.getQuestionType())
                            .options(optionResponses)
                            .build()
            );
        }

        return responses;
    }

    /**
     * Generate exercise từ video
     * Nếu video không tồn tại -> trả response rỗng (không throw)
     */
    @Transactional
    public ExerciseResponse generateFromVideo(GenerateExerciseRequest request) {
        YoutubeVideo video = videoRepository.findById(request.getVideoId()).orElse(null);
        if (video == null) {
            return ExerciseResponse.builder()
                    .exerciseId(null)
                    .totalQuestions(0)
                    .build();
        }

        List<YoutubeTranscript> transcripts =
                transcriptRepository.findByVideoId(request.getVideoId());

        if (transcripts == null || transcripts.isEmpty()) {
            return ExerciseResponse.builder()
                    .exerciseId(null)
                    .totalQuestions(0)
                    .build();
        }

        Exercise exercise = Exercise.builder()
                .video(video)
                .title(request.getTitle())
                .description(request.getDescription())
                .createdAt(Instant.now())
                .build();

        exerciseRepository.save(exercise);

        List<QuestionVideoYoutube> questions = new ArrayList<>();

        for (YoutubeTranscript t : transcripts) {
            try {
                String prompt = """
Bạn là giáo viên tiếng Nhật.
Từ đoạn transcript sau, hãy tạo 1 câu hỏi trắc nghiệm nghe hiểu.

Yêu cầu:
- Câu hỏi bằng tiếng Nhật
- 4 đáp án
- Chỉ 1 đáp án đúng
- Trả về JSON theo format:
{
  "question": "...",
  "options": ["A","B","C","D"],
  "correctIndex": 0
}

Transcript:
"%s"
""".formatted(t.getText());

                String aiResponse = chatbotService.chat(prompt);

                Map<String, Object> result =
                        objectMapper.readValue(aiResponse, Map.class);

                String questionText = (String) result.get("question");
                List<String> options = (List<String>) result.get("options");
                Integer correctIndex = (Integer) result.get("correctIndex");

                QuestionVideoYoutube q = QuestionVideoYoutube.builder()
                        .exercise(exercise)
                        .transcript(t)
                        .questionText(questionText)
                        .questionType("LISTENING_MCQ")
                        .correctOptionIndex(correctIndex)
                        .createdAt(Instant.now())
                        .build();

                questionRepository.save(q);

                List<AnswerOption> optionEntities = new ArrayList<>();
                for (int i = 0; i < options.size(); i++) {
                    optionEntities.add(
                            AnswerOption.builder()
                                    .question(q)
                                    .content(options.get(i))
                                    .optionIndex(i)
                                    .build()
                    );
                }

                answerOptionRepository.saveAll(optionEntities);
                questions.add(q);

            } catch (Exception e) {
                QuestionVideoYoutube fallback = QuestionVideoYoutube.builder()
                        .exercise(exercise)
                        .transcript(t)
                        .questionText("Nghe và chọn đáp án đúng: \"" + t.getText() + "\"")
                        .questionType("LISTENING_MCQ")
                        .correctOptionIndex(0)
                        .createdAt(Instant.now())
                        .build();

                questionRepository.save(fallback);

                List<AnswerOption> fallbackOptions = List.of(
                        AnswerOption.builder().question(fallback).content(t.getText()).optionIndex(0).build(),
                        AnswerOption.builder().question(fallback).content("Sai 1").optionIndex(1).build(),
                        AnswerOption.builder().question(fallback).content("Sai 2").optionIndex(2).build(),
                        AnswerOption.builder().question(fallback).content("Sai 3").optionIndex(3).build()
                );

                answerOptionRepository.saveAll(fallbackOptions);
                questions.add(fallback);
            }
        }

        exercise.setTotalQuestions(questions.size());
        exerciseRepository.save(exercise);

        return ExerciseResponse.builder()
                .exerciseId(exercise.getId())
                .totalQuestions(questions.size())
                .build();
    }
}
