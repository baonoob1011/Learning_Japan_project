package com.example.learningApp.service.kanji;

import com.example.learningApp.dto.request.kanji.CreateKanjiRequest;
import com.example.learningApp.dto.request.kanji.UpdateKanjiRequest;
import com.example.learningApp.dto.response.kanji.KanjiAiResponse;
import com.example.learningApp.dto.response.kanji.KanjiResponse;
import com.example.learningApp.entity.Kanji;
import com.example.learningApp.repository.KanjiRepository;
import com.example.learningApp.service.ai.KanjiAiService;
import com.example.learningApp.service.ai.KanjiStrokeAiService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KanjiService {

    private final KanjiRepository kanjiRepository;
    private final ObjectMapper objectMapper;
    private final KanjiStrokeAiService kanjiStrokeAiService;
    private  final KanjiAiService kanjiAiService;
    // =================================================
    // GET Kanji by ID
    // =================================================

    @Transactional(readOnly = true)
    public KanjiResponse getKanjiById(String id) {

        Kanji kanji = kanjiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kanji not found"));

        return mapToResponse(kanji);
    }

    // =================================================
    // CREATE Kanji
    // =================================================

    @Transactional
    public KanjiResponse createKanji(CreateKanjiRequest request) {

        try {

            String character = request.getCharacter();

            // 1️⃣ Generate meaning + tọa độ
            KanjiAiResponse aiData =
                    kanjiAiService.generateKanjiData(character);

            // 2️⃣ Generate SVG strokes
            List<String> svgStrokes =
                    kanjiStrokeAiService.generateSvgStrokes(character);

            Kanji kanji = new Kanji();
            kanji.setCharacter(character);
            kanji.setMeaning(aiData.getMeaning());
            kanji.setOnyomi(aiData.getOnyomi());
            kanji.setKunyomi(aiData.getKunyomi());

            // Lưu svgStrokes dạng JSON
            String svgJson =
                    objectMapper.writeValueAsString(svgStrokes);
            kanji.setSvgStrokes(svgJson);

            Kanji saved = kanjiRepository.save(kanji);

            return mapToResponse(saved);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create Kanji", e);
        }
    }
    // =================================================
    // GET ALL Kanji
    // =================================================

    @Transactional(readOnly = true)
    public List<KanjiResponse> getAllKanji() {

        return kanjiRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    // =================================================
// DELETE Kanji
// =================================================
    @Transactional
    public void deleteKanji(String id) {

        Kanji kanji = kanjiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kanji not found"));

        kanjiRepository.delete(kanji);
    }
    // =================================================
    // UPDATE Kanji
    // =================================================
    @Transactional
    public KanjiResponse updateKanji(String id, UpdateKanjiRequest request) {

        Kanji kanji = kanjiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kanji not found"));

        if (request.getCharacter() != null && !request.getCharacter().equals(kanji.getCharacter())) {
            // Re-generate strokes if character changed
            try {
                List<String> svgStrokes =
                        kanjiStrokeAiService.generateSvgStrokes(request.getCharacter());
                String svgJson = objectMapper.writeValueAsString(svgStrokes);
                kanji.setSvgStrokes(svgJson);
                kanji.setCharacter(request.getCharacter());
            } catch (Exception e) {
                throw new RuntimeException("Failed to update character strokes", e);
            }
        }

        if (request.getMeaning() != null) kanji.setMeaning(request.getMeaning());
        if (request.getOnyomi() != null) kanji.setOnyomi(request.getOnyomi());
        if (request.getKunyomi() != null) kanji.setKunyomi(request.getKunyomi());

        Kanji saved = kanjiRepository.save(kanji);
        return mapToResponse(saved);
    }

    // =================================================
    // Mapper Entity -> Response
    // =================================================

    private KanjiResponse mapToResponse(Kanji kanji) {

        List<String> strokes = parseSvgStrokes(kanji.getSvgStrokes());

        return KanjiResponse.builder()
                .id(kanji.getId())
                .character(kanji.getCharacter())
                .meaning(kanji.getMeaning())
                .onyomi(kanji.getOnyomi())
                .kunyomi(kanji.getKunyomi())
                .svgStrokes(strokes)
                .build();
    }

    // =================================================
    // Parse JSON svgStrokes
    // =================================================

    private List<String> parseSvgStrokes(String json) {
        try {
            return objectMapper.readValue(
                    json,
                    new TypeReference<List<String>>() {}
            );
        } catch (Exception e) {
            throw new RuntimeException("Invalid svg stroke data");
        }
    }
}
