package com.example.learningApp.service.translate;

import com.atilika.kuromoji.ipadic.Token;
import com.example.learningApp.common.kafka.Producer;
import com.example.learningApp.dto.request.translate.TranslateRequest;
import com.example.learningApp.dto.request.vocab.CreateVocabRequest;
import com.example.learningApp.dto.response.translate.TranslateResponse;
import com.example.learningApp.service.ai.ChatbotService;
import com.example.learningApp.service.translate.interfaces.AudioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class TranslateService {

        private static final String VOCAB_TOPIC = "create-vocab";

        private final SentenceTranslateService sentenceTranslateService;
        private final TokenizeService tokenizeService;
        private final RomajiService romajiService;
        private final AudioService audioService;
        private final ChatbotService chatbotService;
        private final Producer producer;

        public TranslateResponse translate(TranslateRequest request) {

                // 1️⃣ Dịch toàn bộ câu
                CompletableFuture<String> sentenceTranslatedFuture = CompletableFuture
                                .supplyAsync(() -> sentenceTranslateService.translate(
                                                request.getText(),
                                                request.getSourceLang(),
                                                request.getTargetLang()));

                // 2️⃣ Tokenize
                CompletableFuture<Token> tokenFuture = CompletableFuture
                                .supplyAsync(() -> tokenizeService.firstToken(request.getText()));

                // 3️⃣ Dịch nghĩa của từ
                CompletableFuture<String> targetDefsFuture = tokenFuture
                                .thenApplyAsync(token -> sentenceTranslateService.translate(
                                                token.getSurface(),
                                                request.getSourceLang(),
                                                request.getTargetLang()));

                // 4️⃣ Audio
                CompletableFuture<String> audioFuture = tokenFuture
                                .thenApplyAsync(token -> audioService.generateAudio(token.getSurface()));

                // 5️⃣ Example (AI generation)
                CompletableFuture<String> exampleFuture = tokenFuture.thenApplyAsync(token -> {
                        String prompt = "Tạo 1 ví dụ tiếng Nhật tự nhiên chứa từ '" + token.getSurface() +
                                        "'. Trình bày theo định dạng:\n" +
                                        "[Câu tiếng Nhật]\n" +
                                        "[Phiên âm]\n" +
                                        "[Dịch nghĩa]";
                        return chatbotService.chat(prompt);
                });

                CompletableFuture.allOf(
                                sentenceTranslatedFuture,
                                tokenFuture,
                                targetDefsFuture,
                                audioFuture,
                                exampleFuture).join();

                Token token = tokenFuture.join();

                String surface = token.getSurface();
                String reading = token.getReading() != null ? token.getReading() : surface;
                String romaji = romajiService.toRomaji(reading);
                String partOfSpeech = token.getPartOfSpeechLevel1();

                String sentenceTranslated = sentenceTranslatedFuture.join();
                String targetDefs = targetDefsFuture.join();
                String audioUrl = audioFuture.join();
                String example = exampleFuture.join();

                // 6️⃣ Gửi Kafka tạo vocab
                CreateVocabRequest vocabRequest = CreateVocabRequest.builder()
                                .videoId(request.getVideoId())
                                .surface(surface)
                                .reading(reading)
                                .romaji(romaji)
                                .translated(sentenceTranslated)
                                .partOfSpeech(partOfSpeech)
                                .targetDefs(targetDefs)
                                .explain(example)
                                .audioUrl(audioUrl)
                                .build();

                producer.send(VOCAB_TOPIC, request.getVideoId(), vocabRequest);

                // 7️⃣ Trả response (❌ KHÔNG cache Redis)
                return new TranslateResponse(
                                request.getVideoId(),
                                surface,
                                sentenceTranslated,
                                reading,
                                romaji,
                                partOfSpeech,
                                targetDefs,
                                audioUrl,
                                example);
        }
}
