package com.example.learningApp.service.vocab;

import com.example.learningApp.entity.User;
import com.example.learningApp.entity.Vocab;
import com.example.learningApp.entity.VocabPracticeQuestion;
import com.example.learningApp.repository.VocabPracticeQuestionRepository;
import com.example.learningApp.service.ai.ChatbotService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VocabExerciseService {

    ChatbotService chatbotService;
    VocabPracticeQuestionRepository questionRepository;
    ObjectMapper objectMapper = new ObjectMapper();

    @Async
    public void generateAndSaveQuestion(User user, Vocab vocab) {
        try {
            String prompt = String.format(
                "Create a Japanese multiple choice practice question for the word '%s' (meaning: %s, reading: %s). " +
                "The question should be in Vietnamese, focusing on how to use the word in a sentence. " +
                "Return the result ONLY in JSON format with the following keys: " +
                "'questionText' (string), 'options' (array of 4 strings), 'correctAnswer' (string, must be one of the options), 'explanation' (string in Vietnamese).",
                vocab.getSurface(), vocab.getTranslated(), vocab.getReading()
            );

            String response = chatbotService.chat(prompt);
            
            // Clean response to handle potential markdown wrappers
            String jsonStr = response.replace("```json", "").replace("```", "").trim();
            
            Map<String, Object> data = objectMapper.readValue(jsonStr, Map.class);

            VocabPracticeQuestion question = VocabPracticeQuestion.builder()
                .user(user)
                .vocab(vocab)
                .questionText((String) data.get("questionText"))
                .options((List<String>) data.get("options"))
                .correctAnswer((String) data.get("correctAnswer"))
                .explanation((String) data.get("explanation"))
                .build();

            questionRepository.save(question);
            
        } catch (Exception e) {
            System.err.println("Failed to generate AI question for " + vocab.getSurface() + ": " + e.getMessage());
        }
    }

    public List<VocabPracticeQuestion> getExercisesForUser(User user) {
        return questionRepository.findByUser(user);
    }

    @Transactional
    public void generateForBatch(User user, List<Vocab> vocabs) {
        for (Vocab v : vocabs) {
            // Only generate if not already exists for this word today or similar logic
            // For now, just generate
            generateAndSaveQuestion(user, v);
        }
    }
}
