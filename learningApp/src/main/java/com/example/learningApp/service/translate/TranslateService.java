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

import com.example.learningApp.repository.VocabRepository;
import com.example.learningApp.entity.Vocab;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TranslateService {

        private static final String VOCAB_TOPIC = "create-vocab";

        private final SentenceTranslateService sentenceTranslateService;
        private final TokenizeService tokenizeService;
        private final RomajiService romajiService;
        private final AudioService audioService;
        private final ChatbotService chatbotService;
        private final VocabRepository vocabRepository;
        private final Producer producer;

        public TranslateResponse translate(TranslateRequest request) {

                // 1️⃣ Lấy token đầu tiên để trích xuất từ gốc (surface)
                Token token = tokenizeService.firstToken(request.getText());
                String surface = token.getSurface();

                // 2️⃣ Kiểm tra trong DB (Nếu CÓ -> Trả về luôn thông tin trong DB, KHÔNG gọi AI
                // / AWS)
                Optional<Vocab> existingVocab = vocabRepository.findBySurface(surface);
                if (existingVocab.isPresent()) {
                        Vocab vocab = existingVocab.get();
                        return new TranslateResponse(
                                        request.getVideoId(),
                                        vocab.getSurface(),
                                        vocab.getTranslated(), // Dịch câu / đoạn nguyên gốc
                                        vocab.getReading(),
                                        vocab.getRomaji(),
                                        vocab.getPartOfSpeech(),
                                        vocab.getTargetDefs(), // Dịch nghĩa đích
                                        vocab.getAudioUrl(),
                                        vocab.getExample());
                }

                // 3️⃣ Nếu CHƯA CÓ -> Tiến hành xử lý dịch, sinh âm thanh, và chat AI tạo ví dụ
                // (thực thi song song)
                CompletableFuture<String> sentenceTranslatedFuture = CompletableFuture
                                .supplyAsync(() -> sentenceTranslateService.translate(
                                                request.getText(),
                                                request.getSourceLang(),
                                                request.getTargetLang()));

                CompletableFuture<String> targetDefsFuture = CompletableFuture
                                .supplyAsync(() -> sentenceTranslateService.translate(
                                                surface,
                                                request.getSourceLang(),
                                                request.getTargetLang()));

                CompletableFuture<String> audioFuture = CompletableFuture
                                .supplyAsync(() -> audioService.generateAudio(surface));

                CompletableFuture<String> exampleFuture = CompletableFuture.supplyAsync(() -> {
                        String prompt = "Tạo 1 ví dụ tiếng Nhật tự nhiên chứa từ '" + surface +
                                        "'. Trình bày theo định dạng:\n" +
                                        "[Câu tiếng Nhật]\n" +
                                        "[Phiên âm]\n" +
                                        "[Dịch nghĩa]";
                        return chatbotService.chat(prompt);
                });

                // Đợi tất cả completable futures hoàn thành
                CompletableFuture.allOf(
                                sentenceTranslatedFuture,
                                targetDefsFuture,
                                audioFuture,
                                exampleFuture).join();

                String reading = token.getReading() != null ? token.getReading() : surface;
                String romaji = romajiService.toRomaji(reading);
                String partOfSpeech = token.getPartOfSpeechLevel1();

                String sentenceTranslated = sentenceTranslatedFuture.join();
                String targetDefs = targetDefsFuture.join();
                String audioUrl = audioFuture.join();
                String example = exampleFuture.join();

                // 4️⃣ Gửi Kafka tạo vocab mới vào DB
                CreateVocabRequest vocabRequest = CreateVocabRequest.builder()
                                .videoId(request.getVideoId())
                                .surface(surface)
                                .reading(reading)
                                .romaji(romaji)
                                .translated(sentenceTranslated)
                                .partOfSpeech(partOfSpeech)
                                .targetDefs(targetDefs)
                                .explain(example) // (legacy)
                                .example(example) // ✅ lưu vào cột example của DB
                                .audioUrl(audioUrl)
                                .build();

                producer.send(VOCAB_TOPIC, request.getVideoId(), vocabRequest);

                // 5️⃣ Trả response
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
