package com.example.learningApp.service.vocab;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.common.kafka.Producer;
import com.example.learningApp.dto.cache.VocabCache;
import com.example.learningApp.dto.event.VocabSaveExerciseEvent;
import com.example.learningApp.dto.request.translate.TranslateRequest;
import com.example.learningApp.dto.request.vocab.CreateVocabRequest;
import com.example.learningApp.dto.request.vocab.UpdateVocabRequest;
import com.example.learningApp.dto.response.translate.TranslateResponse;
import com.example.learningApp.dto.response.vocab.VocabResponse;
import com.example.learningApp.entity.Exam;
import com.example.learningApp.entity.User;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

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
    UserRepository userRepository;
    VocabCacheRedisService vocabCacheRedisService;
    UserVocabProgressRepository progressRepo;
    TranslateMapper translateMapper;
    VocabCacheMapper vocabCacheMapper;
    Producer kafkaProducer;

    public long resolveSrsReviewIntervalDays(com.example.learningApp.entity.UserVocabProgress progress) {
        // Cực kỳ đơn giản: Quá 3 ngày không ôn là nhắc!
        return 3;
    }

    public boolean isDueForSrsReview(com.example.learningApp.entity.UserVocabProgress progress, LocalDateTime now) {
        if (progress == null) return false;
        
        // Chỉ nhắc những từ đã bắt đầu học (tránh những từ mới tinh chưa bao giờ đụng tới nếu muốn)
        // Nhưng theo yêu cầu bạn muốn nhắc cả chưa thuộc, nên ta sẽ cho NEW cũng nhắc luôn sau 3 ngày
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
        if (total == 0) return null;
        
        long totalUnlearned = forgottenCount + fuzzyCount; // Gộp cả "quên" và "đang học" vào chưa thuộc
        StringBuilder sb = new StringBuilder();
        sb.append("B\u1ea1n c\u00f3 ").append(total).append(" t\u1eeb \u0111\u1ebfn l\u1ecbch \u00f4n: ");
        
        if (totalUnlearned > 0) {
            sb.append(totalUnlearned).append(" t\u1eeb ch\u01b0a thu\u1ed9c");
        }
        
        if (masteredCount > 0) {
            if (totalUnlearned > 0) sb.append(", ");
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
                    resp.setStatus(progress.map(p -> p.getStatus()).orElse(com.example.learningApp.enums.LearningStatus.NEW));
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
                .orElseGet(() -> vocabRepository.save(vocab));

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

        String cacheKey = request.getVideoId() + ":" + request.getText().toLowerCase();

        // 1️⃣ Redis
        VocabCache cache = vocabCacheRedisService.get(cacheKey);
        if (cache != null) {
            return translateMapper.mapWithVideoId(
                    cache,
                    request.getVideoId());
        }

        // 2️⃣ DB
        return vocabRepository.findBySurface(request.getText())
                .map(vocab -> {
                    VocabCache cacheToSave = vocabCacheMapper.toCache(vocab);
                    vocabCacheRedisService.save(cacheKey, cacheToSave);

                    return translateMapper.toTranslateResponse(
                            vocab,
                            request.getVideoId());
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

    public List<VocabResponse> getSavedVocabsOfCurrentUser() {
        var user = finder.userById();
        return user.getSavedVocabs()
                .stream()
                .map(vocab -> {
                    VocabResponse resp = vocabMapper.toVocabResponse(vocab);
                    var progress = progressRepo.findByUserAndVocab(user, vocab);
                    resp.setStatus(progress.map(p -> p.getStatus()).orElse(com.example.learningApp.enums.LearningStatus.NEW));
                    return resp;
                })
                .toList();
    }

    @Transactional
    public void removeVocabForCurrentUser(String surface) {
        var user = finder.userById();
        // 2️⃣ Tìm vocab
        Vocab vocab = finder.vocabBySurface(surface);

        // 3️⃣ Xóa quan hệ
        if (user.getSavedVocabs().remove(vocab)) {
            vocab.getUsers().remove(user); // ✔ giữ consistency 2 chiều
            userRepository.save(user);
        }
    }

    @Transactional
    public void updateVocabMeaning(UpdateVocabRequest request) {

        Vocab vocab = finder.vocabBySurface(request.getSurface());

        // 🔒 CHỈ sửa nghĩa
        vocab.setTranslated(request.getTranslated());

        vocabRepository.save(vocab);
    }

}

