package com.example.learningApp.service.translate;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;
import com.example.learningApp.dto.response.translate.TranslateResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.services.translate.model.TranslateTextRequest;
import software.amazon.awssdk.services.translate.model.TranslateTextResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TranslateService {

    private final TranslateClient translateClient;
    private final Tokenizer tokenizer = new Tokenizer();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public TranslateResponse translate(String text, String source, String target) throws IOException, InterruptedException {

        // 1️⃣ Dịch câu gốc bằng AWS Translate
        TranslateTextResponse response = translateClient.translateText(
                TranslateTextRequest.builder()
                        .text(text)
                        .sourceLanguageCode(source)
                        .targetLanguageCode(target)
                        .build()
        );

        // 2️⃣ Tokenize câu gốc bằng Kuromoji
        List<Token> tokens = tokenizer.tokenize(text);

        // 3️⃣ Reading (Katakana/Hiragana)
        String reading = tokens.stream()
                .map(t -> t.getReading() != null ? t.getReading() : t.getSurface())
                .collect(Collectors.joining(" "));

        // 4️⃣ Romaji
        String romaji = tokens.stream()
                .map(t -> toRomaji(t.getReading() != null ? t.getReading() : t.getSurface()))
                .collect(Collectors.joining(" "));

        // 5️⃣ Giải thích chi tiết từng từ
        String explanation = buildExplanation(tokens, target);

        // 6️⃣ Ví dụ mặc định
        List<TranslateResponse.Example> examples = new ArrayList<>();
        examples.add(new TranslateResponse.Example(text, response.translatedText()));

        // 7️⃣ Trả về response
        return new TranslateResponse(
                text,
                response.translatedText(),
                reading,
                romaji,
                explanation,
                examples
        );
    }

    /**
     * Build explanation kiểu từ điển + dịch sang target language
     */
    private String buildExplanation(List<Token> tokens, String targetLang) throws IOException, InterruptedException {
        List<String> parts = new ArrayList<>();

        for (Token t : tokens) {
            String surface = t.getSurface();
            String reading = t.getReading() != null ? t.getReading() : surface;
            String romaji = toRomaji(reading);

            // Gọi Jisho API để lấy nghĩa tiếng Anh
            String apiUrl = "https://jisho.org/api/v1/search/words?keyword=" + surface;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .GET()
                    .build();

            HttpResponse<String> apiResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(apiResponse.body());

            StringBuilder englishDefs = new StringBuilder();
            StringBuilder targetDefs = new StringBuilder();

            if (root.has("data") && root.get("data").isArray() && root.get("data").size() > 0) {
                JsonNode senses = root.get("data").get(0).get("senses");
                for (JsonNode sense : senses) {
                    // Lấy nghĩa tiếng Anh
                    if (sense.has("english_definitions")) {
                        List<String> eng = new ArrayList<>();
                        for (JsonNode def : sense.get("english_definitions")) {
                            eng.add(def.asText());
                        }
                        String engText = String.join(", ", eng);
                        englishDefs.append(engText).append("; ");

                        // Dịch nghĩa sang target language
                        TranslateTextResponse viResp = translateClient.translateText(
                                TranslateTextRequest.builder()
                                        .text(engText)
                                        .sourceLanguageCode("en")
                                        .targetLanguageCode(targetLang)
                                        .build()
                        );
                        targetDefs.append(viResp.translatedText()).append("; ");
                    }
                }
            }

            String partOfSpeech = t.getPartOfSpeechLevel1();
            parts.add(String.format("%s [%s] (%s) → EN: %s | %s", surface, romaji, partOfSpeech,
                    englishDefs.toString(), targetDefs.toString()));
        }

        return String.join("\n", parts);
    }

    /**
     * Chuyển Katakana → Romaji
     */
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
