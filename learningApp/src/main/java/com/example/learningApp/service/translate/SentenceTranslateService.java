package com.example.learningApp.service.translate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.services.translate.model.TranslateTextRequest;

@Service
@RequiredArgsConstructor
public class SentenceTranslateService {

    private final TranslateClient translateClient;

    public String translate(String text, String source, String target) {
        return translateClient.translateText(
                TranslateTextRequest.builder()
                        .text(text)
                        .sourceLanguageCode(source)
                        .targetLanguageCode(target)
                        .build()
        ).translatedText();
    }
}
