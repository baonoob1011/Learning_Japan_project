package com.example.learningApp.service.translate;

import com.atilika.kuromoji.ipadic.Token;
import com.example.learningApp.common.cache.CacheService;
import com.example.learningApp.common.kafka.Producer;
import com.example.learningApp.dto.cache.VocabCache;
import com.example.learningApp.dto.request.translate.TranslateRequest;
import com.example.learningApp.dto.request.vocab.CreateVocabRequest;
import com.example.learningApp.dto.response.translate.TranslateResponse;
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
    private final CacheService<VocabCache> vocabCacheService;
    private final Producer producer;

    public TranslateResponse translate(TranslateRequest request) {

        /*
         * 1️⃣ Dịch toàn bộ câu
         * - Chạy async để không block thread chính
         * - Độc lập hoàn toàn với tokenize & audio
         */
        CompletableFuture<String> sentenceTranslatedFuture =
                CompletableFuture.supplyAsync(() ->
                        sentenceTranslateService.translate(
                                request.getText(),
                                request.getSourceLang(),
                                request.getTargetLang()
                        )
                );

        /*
         * 2️⃣ Tokenize câu (lấy token đầu tiên)
         * - Là nền tảng cho các bước phía sau (dịch từ + audio)
         */
        CompletableFuture<Token> tokenFuture =
                CompletableFuture.supplyAsync(() ->
                        tokenizeService.firstToken(request.getText())
                );

        /*
         * 3️⃣ Dịch nghĩa của từ (surface)
         * - Phụ thuộc token nên dùng thenApplyAsync
         * - Chỉ chạy khi tokenFuture hoàn thành
         */
        CompletableFuture<String> targetDefsFuture =
                tokenFuture.thenApplyAsync(token ->
                        sentenceTranslateService.translate(
                                token.getSurface(),
                                request.getSourceLang(),
                                request.getTargetLang()
                        )
                );

        /*
         * 4️⃣ Tạo audio cho từ
         * - Cũng phụ thuộc token
         * - Chạy song song với targetDefsFuture
         */
        CompletableFuture<String> audioFuture =
                tokenFuture.thenApplyAsync(token ->
                        audioService.generateAudio(token.getSurface())
                );

        /*
         * 5️⃣ Đồng bộ
         * - Đợi TẤT CẢ future hoàn thành
         * - Nếu 1 task fail → throw exception tại đây
         */
        CompletableFuture.allOf(
                sentenceTranslatedFuture,
                tokenFuture,
                targetDefsFuture,
                audioFuture
        ).join();

        /*
         * 6️⃣ Lấy kết quả từ các Future
         * - join() an toàn vì đã allOf phía trên
         */
        Token token = tokenFuture.join();

        String surface = token.getSurface();
        String reading = token.getReading() != null ? token.getReading() : surface;
        String romaji = romajiService.toRomaji(reading);
        String partOfSpeech = token.getPartOfSpeechLevel1();

        String sentenceTranslated = sentenceTranslatedFuture.join();
        String targetDefs = targetDefsFuture.join();
        String audioUrl = audioFuture.join();

        /*
         * 7️⃣ Gửi Kafka để tạo vocab
         * - Xử lý async ở service khác
         */
        CreateVocabRequest vocabRequest = CreateVocabRequest.builder()
                .videoId(request.getVideoId())
                .surface(surface)
                .reading(reading)
                .romaji(romaji)
                .translated(sentenceTranslated)
                .partOfSpeech(partOfSpeech)
                .targetDefs(targetDefs)
                .audioUrl(audioUrl)
                .build();

        producer.send(VOCAB_TOPIC, request.getVideoId(), vocabRequest);

        /*
         * 8️⃣ Cache Redis
         * - Giúp lần sau load nhanh, không cần dịch + audio lại
         */
        VocabCache cache = new VocabCache(
                request.getVideoId() + "_" + surface,
                surface,
                romaji,
                sentenceTranslated,
                reading,
                targetDefs,
                partOfSpeech,
                audioUrl
        );

        vocabCacheService.save(request.getVideoId() + "_" + surface, cache);

        /*
         * 9️⃣ Trả response cho client
         * - Chỉ trả khi tất cả xử lý đã hoàn thành
         */
        return new TranslateResponse(
                request.getVideoId(),
                surface,
                sentenceTranslated,
                reading,
                romaji,
                partOfSpeech,
                targetDefs,
                audioUrl
        );
    }
}

