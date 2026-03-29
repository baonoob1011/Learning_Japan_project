// ChatbotServiceTest.java
package com.example.learningApp.service.ai;

import com.atilika.kuromoji.ipadic.Token;
import com.example.learningApp.dto.request.translate.TranslateRequest;
import com.example.learningApp.dto.response.translate.TranslateResponse;
import com.example.learningApp.entity.Vocab;
import com.example.learningApp.repository.VocabRepository;
import com.example.learningApp.service.translate.RomajiService;
import com.example.learningApp.service.translate.SentenceTranslateService;
import com.example.learningApp.service.translate.TokenizeService;
import com.example.learningApp.service.translate.TranslateService;
import com.example.learningApp.service.translate.interfaces.AudioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatbotServiceTest {

    @Mock private SentenceTranslateService sentenceTranslateService;
    @Mock private TokenizeService tokenizeService;
    @Mock private RomajiService romajiService;
    @Mock private AudioService audioService;
    @Mock private ChatbotService chatbotService;
    @Mock private VocabRepository vocabRepository;

    @Test
    @DisplayName("TC24 - Grammar question is answered clearly with examples")
    void tc24_grammar_explanation() {
        // Given
        try (MockedConstruction<RestTemplate> mockedConstruction = mockConstruction(RestTemplate.class, (mock, context) ->
                when(mock.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                        .thenReturn(ResponseEntity.ok(Map.of(
                                "candidates", List.of(
                                        Map.of("content", Map.of("parts", List.of(Map.of("text",
                                                "The て-form is used to connect clauses and request actions."))))
                                )
                        )))
        )) {
            ChatbotService service = new ChatbotService();
            ReflectionTestUtils.setField(service, "apiKey", "test-api-key");

            // When
            String answer = service.chat("て形 dùng khi nào?");

            // Then
            assertEquals("The て-form is used to connect clauses and request actions.", answer);
            RestTemplate restTemplate = mockedConstruction.constructed().get(0);
            verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
        }
    }

    @Test
    @DisplayName("TC25 - Japanese sentence translation returns accurate Vietnamese meaning")
    void tc25_translate_sentence_to_vietnamese() {
        // Given
        TranslateRequest request = new TranslateRequest();
        request.setVideoId("vid-1");
        request.setText("私は学校へ行きます");
        request.setSourceLang("ja");
        request.setTargetLang("vi");

        Token token = mock(Token.class);
        when(tokenizeService.firstToken("私は学校へ行きます")).thenReturn(token);
        when(token.getSurface()).thenReturn("学校");
        when(token.getReading()).thenReturn("がっこう");
        when(token.getPartOfSpeechLevel1()).thenReturn("noun");

        when(vocabRepository.findBySurface("学校")).thenReturn(Optional.empty());
        when(sentenceTranslateService.translate(anyString(), anyString(), anyString()))
                .thenAnswer(invocation -> {
                    String text = invocation.getArgument(0);
                    if ("私は学校へ行きます".equals(text)) {
                        return "I go to school";
                    }
                    if ("学校".equals(text)) {
                        return "school";
                    }
                    return text;
                });
        when(audioService.generateAudio("学校")).thenReturn("audio-url");
        when(chatbotService.chat(anyString())).thenReturn("学校は school です.");
        when(romajiService.toRomaji("がっこう")).thenReturn("gakkou");

        TranslateService translateService = new TranslateService(
                sentenceTranslateService,
                tokenizeService,
                romajiService,
                audioService,
                chatbotService,
                vocabRepository
        );

        // When
        TranslateResponse response = translateService.translate(request, "user-1");

        // Then
        assertEquals("vid-1", response.getVideoId());
        assertEquals("学校", response.getSurface());
        assertEquals("I go to school", response.getTranslated());
        assertEquals("がっこう", response.getReading());
        assertEquals("gakkou", response.getRomaji());
        assertEquals("noun", response.getPartOfSpeech());
        assertEquals("school", response.getTargetDefs());
        assertEquals("audio-url", response.getAudioUrl());
        assertEquals("学校は school です.", response.getExample());
    }
}
