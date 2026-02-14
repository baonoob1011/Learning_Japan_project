package com.example.learningApp.service.kanji;

import com.example.learningApp.dto.PointDTO;
import com.example.learningApp.dto.request.kanji.CreateKanjiRequest;
import com.example.learningApp.dto.request.kanji.KanjiStrokeRequest;
import com.example.learningApp.dto.response.kanji.KanjiCheckResponse;
import com.example.learningApp.dto.response.kanji.KanjiResponse;
import com.example.learningApp.entity.Kanji;
import com.example.learningApp.entity.UserKanjiProgress;
import com.example.learningApp.repository.KanjiRepository;
import com.example.learningApp.repository.UserKanjiProgressRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Service
@RequiredArgsConstructor
@Slf4j
public class KanjiService {

    private final KanjiRepository kanjiRepository;
    private final UserKanjiProgressRepository progressRepository;
    private final ObjectMapper objectMapper;

    // =================================================
    // 1️⃣ GET Kanji by ID
    // =================================================

    @Transactional(readOnly = true)
    public KanjiResponse getKanjiById(String id) {

        Kanji kanji = kanjiRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Kanji not found")
                );

        List<List<PointDTO>> strokes =
                parseStrokeData(kanji.getStrokeData());

        return KanjiResponse.builder()
                .id(kanji.getId())
                .character(kanji.getCharacter())
                .meaning(kanji.getMeaning())
                .onyomi(kanji.getOnyomi())
                .kunyomi(kanji.getKunyomi())
                .strokes(strokes)
                .build();
    }

    private List<PointDTO> normalizeStroke(List<PointDTO> stroke) {

        double minX = stroke.stream().mapToDouble(PointDTO::getX).min().orElse(0);
        double minY = stroke.stream().mapToDouble(PointDTO::getY).min().orElse(0);
        double maxX = stroke.stream().mapToDouble(PointDTO::getX).max().orElse(1);
        double maxY = stroke.stream().mapToDouble(PointDTO::getY).max().orElse(1);

        double width = maxX - minX;
        double height = maxY - minY;

        return stroke.stream()
                .map(p -> new PointDTO(
                        (p.getX() - minX) / (width == 0 ? 1 : width),
                        (p.getY() - minY) / (height == 0 ? 1 : height)
                ))
                .toList();
    }

    // =================================================
    // 2️⃣ CHECK Kanji
    // =================================================

    @Transactional
    public KanjiCheckResponse checkKanji(
            String userId,
            KanjiStrokeRequest request
    ) {

        log.info("===== CHECK KANJI START =====");
        log.info("UserId: {}", userId);
        log.info("KanjiId: {}", request.getKanjiId());

        Kanji kanji = kanjiRepository.findById(request.getKanjiId())
                .orElseThrow(() -> new RuntimeException("Kanji not found"));

        List<List<PointDTO>> originalStrokes =
                parseStrokeData(kanji.getStrokeData());

        List<List<PointDTO>> userStrokes =
                request.getStrokes();

        log.info("Expected strokes: {}", originalStrokes.size());
        log.info("User strokes: {}",
                userStrokes != null ? userStrokes.size() : 0);

        double score = calculateScore(
                userStrokes,
                originalStrokes
        );

        log.info("Calculated score: {}", score);

        boolean correct = score >= 80;

        log.info("Is correct: {}", correct);
        log.info("===== CHECK KANJI END =====");

        saveOrUpdateProgress(
                userId,
                kanji.getId(),
                score,
                correct
        );

        return KanjiCheckResponse.builder()
                .correct(correct)
                .score(score)
                .expectedStrokeCount(originalStrokes.size())
                .userStrokeCount(
                        userStrokes != null ? userStrokes.size() : 0
                )
                .build();
    }


    // =================================================
    // Parse JSON Stroke
    // =================================================

    private List<List<PointDTO>> parseStrokeData(String strokeJson) {
        try {
            return objectMapper.readValue(
                    strokeJson,
                    new TypeReference<List<List<PointDTO>>>() {}
            );
        } catch (Exception e) {
            throw new RuntimeException("Invalid stroke data");
        }
    }

    // =================================================
    // Score Algorithm
    // =================================================

    private double calculateScore(
            List<List<PointDTO>> userStrokes,
            List<List<PointDTO>> originalStrokes
    ) {

        if (userStrokes == null || userStrokes.isEmpty())
            return 0;

        if (userStrokes.size() != originalStrokes.size())
            return 0;

        double totalScore = 0;

        for (int i = 0; i < userStrokes.size(); i++) {

            List<PointDTO> user =
                    normalizeStroke(userStrokes.get(i));

            List<PointDTO> original =
                    normalizeStroke(originalStrokes.get(i));

            int minSize = Math.min(user.size(), original.size());
            double strokeScore = 0;

            for (int j = 0; j < minSize; j++) {

                double dx = user.get(j).getX() - original.get(j).getX();
                double dy = user.get(j).getY() - original.get(j).getY();

                double distance = Math.sqrt(dx * dx + dy * dy);

                // tăng tolerance lên
                if (distance <= 0.2) {
                    strokeScore++;
                }
            }

            totalScore += (strokeScore / minSize);
        }

        return Math.round(
                (totalScore / userStrokes.size()) * 100
        );
    }

    // =================================================
// 3️⃣ GET ALL KANJI
// =================================================

    @Transactional(readOnly = true)
    public List<KanjiResponse> getAllKanji() {

        return kanjiRepository.findAll()
                .stream()
                .map(kanji -> KanjiResponse.builder()
                        .id(kanji.getId())
                        .character(kanji.getCharacter())
                        .meaning(kanji.getMeaning())
                        .onyomi(kanji.getOnyomi())
                        .kunyomi(kanji.getKunyomi())
                        .strokes(parseStrokeData(kanji.getStrokeData()))
                        .build()
                )
                .toList();
    }


// =================================================
// 4️⃣ CREATE KANJI
// =================================================

    @Transactional
    public KanjiResponse createKanji(CreateKanjiRequest request) {

        Kanji kanji = new Kanji();
        kanji.setCharacter(request.getCharacter());
        kanji.setMeaning(request.getMeaning());
        kanji.setOnyomi(request.getOnyomi());
        kanji.setKunyomi(request.getKunyomi());
        kanji.setStrokeData(request.getStrokeData());

        Kanji saved = kanjiRepository.save(kanji);

        return KanjiResponse.builder()
                .id(saved.getId())
                .character(saved.getCharacter())
                .meaning(saved.getMeaning())
                .onyomi(saved.getOnyomi())
                .kunyomi(saved.getKunyomi())
                .strokes(parseStrokeData(saved.getStrokeData()))
                .build();
    }


    // =================================================
    // Save Progress
    // =================================================

    private void saveOrUpdateProgress(
            String userId,
            String kanjiId,
            double score,
            boolean completed
    ) {

        UserKanjiProgress progress =
                progressRepository
                        .findByUserIdAndKanjiId(userId, kanjiId)
                        .orElse(
                                UserKanjiProgress.builder()
                                        .id(UUID.randomUUID().toString())
                                        .userId(userId)
                                        .kanjiId(kanjiId)
                                        .build()
                        );

        progress.setScore(score);
        progress.setCompleted(completed);
        progress.setUpdatedAt(LocalDateTime.now());

        progressRepository.save(progress);
    }
}
