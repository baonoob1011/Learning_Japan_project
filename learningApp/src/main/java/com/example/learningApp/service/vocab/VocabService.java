package com.example.learningApp.service.vocab;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.common.PageResponse;
import com.example.learningApp.common.kafka.Producer;
import com.example.learningApp.dto.cache.VocabCache;
import com.example.learningApp.dto.event.VocabSaveExerciseEvent;
import com.example.learningApp.dto.request.translate.TranslateRequest;
import com.example.learningApp.dto.request.vocab.CreateManualVocabRequest;
import com.example.learningApp.dto.request.vocab.CreateVocabRequest;
import com.example.learningApp.dto.request.vocab.UpdateVocabRequest;
import com.example.learningApp.dto.response.translate.TranslateResponse;
import com.example.learningApp.dto.response.vocab.VocabResponse;
import com.example.learningApp.entity.Exam;
import com.example.learningApp.entity.User;
import com.example.learningApp.entity.UserVocabProgress;
import com.example.learningApp.entity.Vocab;
import com.example.learningApp.entity.YoutubeVideo;
import com.example.learningApp.exception.NotFoundException;
import com.example.learningApp.mapper.TranslateMapper;
import com.example.learningApp.mapper.VocabCacheMapper;
import com.example.learningApp.mapper.VocabMapper;
import com.example.learningApp.repository.UserRepository;
import com.example.learningApp.repository.UserVocabProgressRepository;
import com.example.learningApp.repository.VocabRepository;
import com.example.learningApp.repository.YoutubeVideoRepository;
import com.example.learningApp.service.translate.TranslateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.awt.SystemColor.text;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VocabService {
    VocabRepository vocabRepository;
    VocabMapper vocabMapper;
    EntityFinder finder;
    YoutubeVideoRepository youtubeVideoRepository;
    TranslateService translateService;
    com.example.learningApp.service.translate.TokenizeService tokenizeService;
    UserRepository userRepository;
    VocabCacheRedisService vocabCacheRedisService;
    UserVocabProgressRepository progressRepo;
    TranslateMapper translateMapper;
    VocabCacheMapper vocabCacheMapper;
    Producer kafkaProducer;
    com.example.learningApp.repository.ReviewSessionItemRepository reviewSessionItemRepository;

    public long resolveSrsReviewIntervalDays(com.example.learningApp.entity.UserVocabProgress progress) {
        // Cực kỳ đơn giản: Quá 3 ngày không ôn là nhắc!
        return 3;
    }

    public boolean isDueForSrsReview(com.example.learningApp.entity.UserVocabProgress progress, LocalDateTime now) {
        if (progress == null)
            return false;

        // Chỉ nhắc những từ đã bắt đầu học (tránh những từ mới tinh chưa bao giờ đụng
        // tới nếu muốn)
        // Nhưng theo yêu cầu bạn muốn nhắc cả chưa thuộc, nên ta sẽ cho NEW cũng nhắc
        // luôn sau 3 ngày
        LocalDateTime lastReviewedAt = progress.getLastReviewedAt();
        if (lastReviewedAt == null) {
            // Nếu chưa bao giờ Review, coi như CreatedAt là điểm bắt đầu
            if (progress.getCreatedAt() != null) {
                return progress.getCreatedAt().plusDays(3).isBefore(now);
            }
            return true;
        }

        return lastReviewedAt.plusDays(3).isBefore(now);
    }

    public String buildSrsReminderMessage(long forgottenCount, long fuzzyCount, long masteredCount) {
        long total = forgottenCount + fuzzyCount + masteredCount;
        if (total == 0)
            return null;

        long totalUnlearned = forgottenCount + fuzzyCount; // Gộp cả "quên" và "đang học" vào chưa thuộc
        StringBuilder sb = new StringBuilder();
        sb.append("B\u1ea1n c\u00f3 ").append(total).append(" t\u1eeb \u0111\u1ebfn l\u1ecbch \u00f4n: ");

        if (totalUnlearned > 0) {
            sb.append(totalUnlearned).append(" t\u1eeb ch\u01b0a thu\u1ed9c");
        }

        if (masteredCount > 0) {
            if (totalUnlearned > 0)
                sb.append(", ");
            sb.append(masteredCount).append(" t\u1eeb \u0111\u00e3 thu\u1ed9c");
        }

        sb.append(".");
        return sb.toString();
    }

    public List<VocabResponse> getSavedVocabsOfCurrentUserByVideo(String videoId) {
        var user = finder.userById();
        List<Vocab> vocabs = vocabRepository.findSavedVocabsByUserAndVideo(user.getId(), videoId);
        return vocabs.stream()
                .map(vocab -> {
                    VocabResponse resp = vocabMapper.toVocabResponse(vocab);
                    var progress = progressRepo.findByUserAndVocab(user, vocab);
                    if (progress.isPresent()) {
                        resp.setStatus(progress.get().getStatus());
                        resp.setPersonalNote(progress.get().getPersonalNote());
                        resp.setCustomTranslated(progress.get().getCustomTranslated());
                        resp.setPersonalExample(progress.get().getPersonalExample());
                        resp.setPersonalTags(progress.get().getPersonalTags());
                    } else {
                        resp.setStatus(com.example.learningApp.enums.LearningStatus.NEW);
                    }
                    return resp;
                })
                .toList();
    }

    @Transactional
    public void createVocab(CreateVocabRequest request) {
        Vocab vocab = vocabMapper.toVocab(request);
        var video = finder.videoById(request.getVideoId());
        var user = finder.userById();

        Vocab savedVocab = vocabRepository.findBySurface(vocab.getSurface())
                .orElseGet(() -> {
                    Vocab saved = vocabRepository.save(vocab);
                    // 🚀 Kích hoạt Enrichment (Audio + Example) song song qua Kafka
                    kafkaProducer.send("enrich-vocab", saved.getId(), saved.getId());
                    return saved;
                });

        // Lưu vào Video
        if (!video.getVocabs().contains(savedVocab)) {
            video.getVocabs().add(savedVocab);
        }
        savedVocab.getVideos().add(video);
        youtubeVideoRepository.save(video);

        // Lưu vào User (để hiện trong Tab Từ vựng)
        if (!user.getSavedVocabs().contains(savedVocab)) {
            user.getSavedVocabs().add(savedVocab);
            userRepository.save(user);
        }
    }

    public TranslateResponse findOrTranslate(TranslateRequest request) {

        // Dùng tokenizer để bóc tách từ gốc (surface) chính xác
        com.atilika.kuromoji.ipadic.Token token = tokenizeService.firstToken(request.getText());
        String surface = (token != null) ? token.getSurface() : request.getText();
        String cacheKey = request.getVideoId() + ":" + surface.toLowerCase();

        // 1️⃣ Redis
        VocabCache cache = vocabCacheRedisService.get(cacheKey);
        if (cache != null) {
            return translateMapper.mapWithVideoId(cache, request.getVideoId());
        }

        // 2️⃣ DB
        return vocabRepository.findBySurface(surface)
                .map(vocab -> {
                    VocabCache cacheToSave = vocabCacheMapper.toCache(vocab);
                    vocabCacheRedisService.save(cacheKey, cacheToSave);

                    return translateMapper.toTranslateResponse(vocab, request.getVideoId());
                })
                // 3️⃣ Không có → translate mới
                .orElseGet(() -> translateService.translate(request));
    }

    public Void saveVocabForCurrentUser(String surface) {

        var user = finder.userById();
        Vocab vocab = finder.vocabBySurface(surface);
        // 2️⃣ Tránh lưu trùng
        if (user.getSavedVocabs().contains(vocab)) {
            return null;
        }

        // 3️⃣ Lưu quan hệ
        user.getSavedVocabs().add(vocab);
        userRepository.save(user);

        // 4️⃣ Gọi AI tạo bài tập qua Kafka
        kafkaProducer.send("vocab-save-exercise", user.getId(), VocabSaveExerciseEvent.builder()
                .userId(user.getId())
                .vocabId(vocab.getId())
                .surface(vocab.getSurface())
                .build());

        return null;
    }

    @Transactional
    public VocabResponse createManualVocabForCurrentUser(CreateManualVocabRequest request) {
        User user = finder.userById();
        String normalizedSurface = request.getSurface().trim();

        Vocab vocab = vocabRepository.findBySurface(normalizedSurface)
                .orElseGet(() -> Vocab.builder()
                        .surface(normalizedSurface)
                        .translated(trimToNull(request.getTranslated()))
                        .reading(trimToNull(request.getReading()))
                        .romaji(trimToNull(request.getRomaji()))
                        .partOfSpeech(trimToNull(request.getPartOfSpeech()))
                        .build());

        boolean changed = false;
        changed |= fillIfBlank(vocab::getTranslated, vocab::setTranslated, request.getTranslated());
        changed |= fillIfBlank(vocab::getReading, vocab::setReading, request.getReading());
        changed |= fillIfBlank(vocab::getRomaji, vocab::setRomaji, request.getRomaji());
        changed |= fillIfBlank(vocab::getPartOfSpeech, vocab::setPartOfSpeech, request.getPartOfSpeech());

        if (vocab.getId() == null || changed) {
            vocab = vocabRepository.save(vocab);
            // 🚀 Bổ sung audio & ví dụ cho từ vừa tạo nếu chưa có
            kafkaProducer.send("enrich-vocab", vocab.getId(), vocab.getId());
        }

        if (!user.getSavedVocabs().contains(vocab)) {
            user.getSavedVocabs().add(vocab);
            userRepository.save(user);
        }

        VocabResponse response = vocabMapper.toVocabResponse(vocab);
        progressRepo.findByUserAndVocab(user, vocab).ifPresent(p -> {
            response.setStatus(p.getStatus());
            response.setPersonalNote(p.getPersonalNote());
            response.setCustomTranslated(p.getCustomTranslated());
            response.setPersonalExample(p.getPersonalExample());
            response.setPersonalTags(p.getPersonalTags());
        });
        if (response.getStatus() == null) {
            response.setStatus(com.example.learningApp.enums.LearningStatus.NEW);
        }
        return response;
    }

    public List<VocabResponse> getSavedVocabsOfCurrentUser() {
        var user = finder.userById();
        return vocabRepository.findAllByUsers_Id(user.getId())
                .stream()
                .map(vocab -> {
                    VocabResponse resp = vocabMapper.toVocabResponse(vocab);
                    var progress = progressRepo.findByUserAndVocab(user, vocab);
                    if (progress.isPresent()) {
                        resp.setStatus(progress.get().getStatus());
                        resp.setNextReviewAt(progress.get().getNextReviewAt());
                        // Map personalization fields
                        resp.setPersonalNote(progress.get().getPersonalNote());
                        resp.setCustomTranslated(progress.get().getCustomTranslated());
                        resp.setPersonalExample(progress.get().getPersonalExample());
                        resp.setPersonalTags(progress.get().getPersonalTags());
                    } else {
                        resp.setStatus(com.example.learningApp.enums.LearningStatus.NEW);
                    }
                    return resp;
                })
                .toList();
    }

    @Transactional
    public void removeVocabForCurrentUser(String surface) {
        var user = finder.userById();
        Vocab vocab = finder.vocabBySurface(surface);

        // ✅ 1. Dọn dẹp dữ liệu học tập (Progress & Review Sessions)
        progressRepo.findByUserAndVocab(user, vocab).ifPresent(progress -> {
            reviewSessionItemRepository.deleteByWordProgress_Id(progress.getId());
            progressRepo.delete(progress);
        });

        // ✅ 2. Xóa quan hệ Many-to-Many
        if (user.getSavedVocabs().remove(vocab)) {
            vocab.getUsers().remove(user);
            userRepository.save(user);
        }
    }

    @Transactional
    public void updateVocabMeaning(UpdateVocabRequest request) {
        User user = finder.userById();
        Vocab vocab = finder.vocabBySurface(request.getSurface());

        // CHỈ cập nhật thông tin cá nhân của User không ảnh hưởng từ vựng gốc
        var progress = progressRepo.findByUserAndVocab(user, vocab)
                .orElseGet(() -> {
                    // Nếu chưa có Progress, tạo mới (tương tự như Save)
                    var p = com.example.learningApp.entity.UserVocabProgress.builder()
                            .user(user)
                            .vocab(vocab)
                            .status(com.example.learningApp.enums.LearningStatus.NEW)
                            .build();
                    if (!user.getSavedVocabs().contains(vocab)) {
                        user.getSavedVocabs().add(vocab);
                        userRepository.save(user);
                    }
                    return progressRepo.save(p);
                });

        if (request.getCustomTranslated() != null)
            progress.setCustomTranslated(request.getCustomTranslated());
        if (request.getPersonalNote() != null)
            progress.setPersonalNote(request.getPersonalNote());
        if (request.getPersonalExample() != null)
            progress.setPersonalExample(request.getPersonalExample());
        if (request.getPersonalTags() != null)
            progress.setPersonalTags(request.getPersonalTags());
        if (request.getStatus() != null)
            progress.setStatus(request.getStatus());

        progressRepo.save(progress);
    }

    // ─── ADMIN METHODS ──────────────────────────────────────────────────────────

    public PageResponse<VocabResponse> getAllVocabsManager(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("surface").ascending());
        Page<Vocab> vocabPage;

        if (search == null || search.trim().isEmpty()) {
            vocabPage = vocabRepository.findAll(pageable);
        } else {
            String s = search.trim();
            vocabPage = vocabRepository
                    .findBySurfaceContainingIgnoreCaseOrReadingContainingIgnoreCaseOrTranslatedContainingIgnoreCase(
                            s, s, s, pageable);
        }

        List<VocabResponse> data = vocabPage.getContent().stream()
                .map(vocabMapper::toVocabResponse)
                .toList();

        return PageResponse.<VocabResponse>builder()
                .page(page)
                .totalPages(vocabPage.getTotalPages())
                .size(vocabPage.getSize())
                .totalElements(vocabPage.getTotalElements())
                .data(data)
                .build();
    }

    @Transactional
    public VocabResponse adminCreateVocab(VocabResponse request) {
        Vocab vocab = Vocab.builder()
                .surface(request.getSurface())
                .reading(request.getReading())
                .romaji(request.getRomaji())
                .translated(request.getTranslated())
                .partOfSpeech(request.getPartOfSpeech())
                .audioUrl(request.getAudioUrl())
                .build();
        return vocabMapper.toVocabResponse(vocabRepository.save(vocab));
    }

    @Transactional
    public VocabResponse adminUpdateVocab(String id, VocabResponse request) {
        Vocab vocab = vocabRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Vocab not found with id: " + id));

        vocab.setSurface(request.getSurface());
        vocab.setReading(request.getReading());
        vocab.setRomaji(request.getRomaji());
        vocab.setTranslated(request.getTranslated());
        vocab.setPartOfSpeech(request.getPartOfSpeech());
        vocab.setAudioUrl(request.getAudioUrl());

        return vocabMapper.toVocabResponse(vocabRepository.save(vocab));
    }

    @Transactional
    public void adminDeleteVocab(String id) {
        if (!vocabRepository.existsById(id)) {
            throw new NotFoundException("Vocab not found with id: " + id);
        }
        vocabRepository.deleteById(id);
    }

    private static String trimToNull(String value) {
        if (value == null)
            return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean fillIfBlank(Supplier<String> getter, Consumer<String> setter, String incoming) {
        String current = trimToNull(getter.get());
        String candidate = trimToNull(incoming);
        if (candidate == null || current != null) {
            return false;
        }
        setter.accept(candidate);
        return true;
    }

}
