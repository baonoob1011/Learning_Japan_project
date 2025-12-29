package com.example.learningApp.service.translate;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;
import com.example.learningApp.component.kafka.Producer;
import com.example.learningApp.dto.cache.VocabCache;
import com.example.learningApp.dto.request.vocab.CreateVocabRequest;
import com.example.learningApp.dto.response.translate.TranslateResponse;
import com.example.learningApp.repository.VocabRepository;
import com.example.learningApp.service.cloud.S3Service;
import com.example.learningApp.service.vocab.VocabService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.OutputFormat;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechResponse;
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.services.translate.model.TranslateTextRequest;
import software.amazon.awssdk.services.translate.model.TranslateTextResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
@Service
@RequiredArgsConstructor
public class TranslateService {

    private final TranslateClient translateClient;
    private final PollyClient pollyClient;
    private final S3Service s3Service;
    private final Producer producer;
    private final Tokenizer tokenizer = new Tokenizer();
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String VOCAB_TOPIC = "create-vocab";

    public TranslateResponse translate(String videoId, String text, String source, String target)
            throws IOException, InterruptedException {

        // 1️⃣ Dịch cả câu
        TranslateTextResponse sentenceTranslate =
                translateClient.translateText(
                        TranslateTextRequest.builder()
                                .text(text)
                                .sourceLanguageCode(source)
                                .targetLanguageCode(target)
                                .build()
                );

        // 2️⃣ Tokenize
        List<Token> tokens = tokenizer.tokenize(text);
        Token token = tokens.get(0);

        String surface = token.getSurface();
        String reading = token.getReading() != null ? token.getReading() : surface;
        String romaji = toRomaji(reading);
        String partOfSpeech = token.getPartOfSpeechLevel1();

        // 3️⃣ targetDefs = nghĩa của từ
        TranslateTextResponse defTranslate =
                translateClient.translateText(
                        TranslateTextRequest.builder()
                                .text(surface)
                                .sourceLanguageCode("ja")
                                .targetLanguageCode(target)
                                .build()
                );

        String targetDefs = defTranslate.translatedText();

        // 4️⃣ Polly audio
        byte[] audioBytes;
        try (var res = pollyClient.synthesizeSpeech(
                SynthesizeSpeechRequest.builder()
                        .text(surface)
                        .voiceId("Mizuki")
                        .outputFormat(OutputFormat.MP3)
                        .build()
        )) {
            audioBytes = res.readAllBytes();
        }

        String audioUrl = s3Service.uploadBytes(audioBytes, "tts", ".mp3");

        // 5️⃣ Kafka
        CreateVocabRequest vocab = CreateVocabRequest.builder()
                .videoId(videoId)
                .surface(surface)
                .reading(reading)
                .romaji(romaji)
                .translated(sentenceTranslate.translatedText())
                .partOfSpeech(partOfSpeech)
                .targetDefs(targetDefs)
                .audioUrl(audioUrl)
                .build();

        producer.send(VOCAB_TOPIC, videoId, vocab);

        // 6️⃣ Redis cache
        VocabCache cache = new VocabCache(
                videoId + "_" + surface,
                surface,
                romaji,
                sentenceTranslate.translatedText(),
                reading,
                targetDefs,
                partOfSpeech,
                audioUrl
        );

        redisTemplate.opsForValue().set(
                "vocabCache:" + videoId + "_" + surface,
                cache,
                Duration.ofHours(1)
        );

        // 7️⃣ Response
        return new TranslateResponse(
                videoId,
                surface,
                sentenceTranslate.translatedText(),
                reading,
                romaji,
                partOfSpeech,
                targetDefs,
                audioUrl
        );
    }


//    // ================= JISHO =================
//
//    private String getExplainFromJisho(String surface) throws IOException, InterruptedException {
//        String url = "https://jisho.org/api/v1/search/words?keyword=" +
//                URLEncoder.encode(surface, StandardCharsets.UTF_8);
//
//        HttpClient client = HttpClient.newHttpClient();
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(url))
//                .timeout(Duration.ofSeconds(10))
//                .GET()
//                .build();
//
//        HttpResponse<String> response =
//                client.send(request, HttpResponse.BodyHandlers.ofString());
//
//        JsonNode data = objectMapper.readTree(response.body()).get("data");
//        if (data == null || data.isEmpty()) {
//            return "Không tìm thấy giải thích";
//        }
//
//        for (JsonNode entry : data) {
//            for (JsonNode jp : entry.get("japanese")) {
//                if (jp.has("word") && surface.equals(jp.get("word").asText())
//                        || jp.has("reading") && surface.equals(jp.get("reading").asText())) {
//
//                    JsonNode senses = entry.get("senses");
//                    if (senses != null && senses.size() > 0) {
//                        JsonNode sense = senses.get(0);
//                        return sense.get("english_definitions")
//                                .elements()
//                                .next()
//                                .asText();
//                    }
//                }
//            }
//        }
//
//        return surface.matches("[ァ-ヴー]+")
//                ? "Danh từ mượn từ tiếng Anh"
//                : "Không tìm thấy nghĩa phù hợp";
//    }


    private String toRomaji(String katakana) {
        return katakana
                .replace("ア", "a").replace("イ", "i").replace("ウ", "u")
                .replace("エ", "e").replace("オ", "o")
                .replace("カ", "ka").replace("キ", "ki").replace("ク", "ku")
                .replace("ケ", "ke").replace("コ", "ko")
                .replace("サ", "sa").replace("シ", "shi").replace("ス", "su")
                .replace("セ", "se").replace("ソ", "so")
                .replace("タ", "ta").replace("チ", "chi").replace("ツ", "tsu")
                .replace("テ", "te").replace("ト", "to")
                .replace("ナ", "na").replace("ニ", "ni").replace("ヌ", "nu")
                .replace("ネ", "ne").replace("ノ", "no")
                .replace("ハ", "ha").replace("ヒ", "hi").replace("フ", "fu")
                .replace("ヘ", "he").replace("ホ", "ho")
                .replace("マ", "ma").replace("ミ", "mi").replace("ム", "mu")
                .replace("メ", "me").replace("モ", "mo")
                .replace("ヤ", "ya").replace("ユ", "yu").replace("ヨ", "yo")
                .replace("ラ", "ra").replace("リ", "ri").replace("ル", "ru")
                .replace("レ", "re").replace("ロ", "ro")
                .replace("ワ", "wa").replace("ヲ", "wo").replace("ン", "n");
    }
}
