package com.example.learningApp.service.translate;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;
import com.example.learningApp.component.kafka.Producer;
import com.example.learningApp.dto.request.vocab.CreateVocabRequest;
import com.example.learningApp.dto.response.translate.TranslateResponse;
import com.example.learningApp.repository.VocabRepository;
import com.example.learningApp.service.cloud.S3Service;
import com.example.learningApp.service.vocab.VocabService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
    private final PollyClient pollyClient;
    private final S3Service s3Service;
    private final Producer producer;

    private final Tokenizer tokenizer = new Tokenizer();
    private static final String VOCAB_TOPIC = "create-vocab";

    public TranslateResponse translate(String videoId, String text, String source, String target) throws IOException {

        TranslateTextResponse translateResp = translateClient.translateText(
                TranslateTextRequest.builder()
                        .text(text)
                        .sourceLanguageCode(source)
                        .targetLanguageCode(target)
                        .build()
        );

        List<Token> tokens = tokenizer.tokenize(text);

        String reading = tokens.stream()
                .map(t -> t.getReading() != null ? t.getReading() : t.getSurface())
                .collect(Collectors.joining(" "));

        String romaji = tokens.stream()
                .map(t -> toRomaji(t.getReading() != null ? t.getReading() : t.getSurface()))
                .collect(Collectors.joining(" "));

        Token firstToken = tokens.get(0);
        String surface = firstToken.getSurface();
        String partOfSpeech = firstToken.getPartOfSpeechLevel1();

        TranslateTextResponse targetDefResp = translateClient.translateText(
                TranslateTextRequest.builder()
                        .text(surface)
                        .sourceLanguageCode("ja")
                        .targetLanguageCode(target)
                        .build()
        );
        String targetDefs = targetDefResp.translatedText();

        TranslateTextResponse explainResp = translateClient.translateText(
                TranslateTextRequest.builder()
                        .text(surface)
                        .sourceLanguageCode("ja")
                        .targetLanguageCode("vi")
                        .build()
        );
        String explain = explainResp.translatedText();

        SynthesizeSpeechRequest speechRequest = SynthesizeSpeechRequest.builder()
                .text(surface)
                .voiceId("Mizuki")
                .outputFormat(OutputFormat.MP3)
                .build();

        byte[] audioBytes;
        try (software.amazon.awssdk.core.ResponseInputStream<SynthesizeSpeechResponse> speechResponse =
                     pollyClient.synthesizeSpeech(speechRequest)) {
            audioBytes = speechResponse.readAllBytes();
        }

        String audioUrl = s3Service.uploadBytes(audioBytes, "tts", ".mp3");

        CreateVocabRequest vocabRequest = CreateVocabRequest.builder()
                .videoId(videoId)
                .surface(surface)
                .romaji(romaji)
                .reading(reading)
                .translated(translateResp.translatedText())
                .partOfSpeech(partOfSpeech)
                .targetDefs(targetDefs)
                .explain(explain)
                .audioUrl(audioUrl)
                .build();

       producer.send(VOCAB_TOPIC, videoId, vocabRequest);

        return new TranslateResponse(
                videoId,
                surface,
                translateResp.translatedText(),
                reading,
                romaji,
                partOfSpeech,
                targetDefs,
                audioUrl,
                explain
        );
    }

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
