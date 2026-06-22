// KanjiServiceTest.java
package com.example.learningApp.service.kanji;

import com.example.learningApp.dto.request.kanji.CreateKanjiRequest;
import com.example.learningApp.dto.request.kanji.UpdateKanjiRequest;
import com.example.learningApp.dto.response.kanji.KanjiAiResponse;
import com.example.learningApp.dto.response.kanji.KanjiResponse;
import com.example.learningApp.entity.Kanji;
import com.example.learningApp.repository.KanjiRepository;
import com.example.learningApp.service.ai.KanjiAiService;
import com.example.learningApp.service.ai.KanjiStrokeAiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KanjiServiceTest {

    @Mock private KanjiRepository kanjiRepository;
    @Spy private ObjectMapper objectMapper = new ObjectMapper();
    @Mock private KanjiStrokeAiService kanjiStrokeAiService;
    @Mock private KanjiAiService kanjiAiService;

    @InjectMocks
    private KanjiService kanjiService;

    @Test
    @DisplayName("TC22 - AI animation is generated with stroke order guidance")
    void tc22_animation_guidance() {
        // Given
        CreateKanjiRequest request = new CreateKanjiRequest();
        request.setCharacter("日");

        KanjiAiResponse aiResponse = new KanjiAiResponse();
        aiResponse.setMeaning("sun");
        aiResponse.setOnyomi("ニチ");
        aiResponse.setKunyomi("ひ");

        when(kanjiAiService.generateKanjiData("日")).thenReturn(aiResponse);
        when(kanjiStrokeAiService.generateSvgStrokes("日")).thenReturn(List.of("stroke-1", "stroke-2"));
        when(kanjiRepository.save(any(Kanji.class))).thenAnswer(invocation -> {
            Kanji kanji = invocation.getArgument(0);
            kanji.setId("k1");
            return kanji;
        });

        // When
        KanjiResponse response = kanjiService.createKanji(request);

        // Then
        assertEquals("k1", response.getId());
        assertEquals("日", response.getCharacter());
        assertEquals("sun", response.getMeaning());
        assertEquals(List.of("stroke-1", "stroke-2"), response.getSvgStrokes());
    }

    @Test
    @DisplayName("TC23 - Canvas writing updates Kanji strokes and content")
    void tc23_canvas_practice_write() {
        // Given
        Kanji kanji = new Kanji();
        kanji.setId("k1");
        kanji.setCharacter("日");
        kanji.setMeaning("sun");
        kanji.setOnyomi("ニチ");
        kanji.setKunyomi("ひ");
        kanji.setSvgStrokes("[\"old-stroke\"]");

        UpdateKanjiRequest request = new UpdateKanjiRequest();
        request.setCharacter("月");
        request.setMeaning("moon");
        request.setOnyomi("ゲツ");
        request.setKunyomi("つき");

        when(kanjiRepository.findById("k1")).thenReturn(Optional.of(kanji));
        when(kanjiStrokeAiService.generateSvgStrokes("月")).thenReturn(List.of("stroke-a", "stroke-b"));
        when(kanjiRepository.save(any(Kanji.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        KanjiResponse response = kanjiService.updateKanji("k1", request);

        // Then
        assertEquals("月", response.getCharacter());
        assertEquals("moon", response.getMeaning());
        assertEquals(List.of("stroke-a", "stroke-b"), response.getSvgStrokes());
        verify(kanjiStrokeAiService).generateSvgStrokes("月");
    }
}
