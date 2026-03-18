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
    TranslateMapper translateMapper;
    VocabCacheMapper vocabCacheMapper;
    Producer kafkaProducer;

    public List<VocabResponse> getSavedVocabsOfCurrentUserByVideo(String videoId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        List<Vocab> vocabs = vocabRepository.findSavedVocabsByUserAndVideo(userId, videoId);
        return vocabs.stream()
                .map(vocabMapper::toVocabResponse)
                .toList();
    }

    @Transactional
    public void createVocab(CreateVocabRequest request) {
        Vocab vocab = vocabMapper.toVocab(request);
        var video = finder.videoById(request.getVideoId());
        Vocab savedVocab = vocabRepository.findBySurface(vocab.getSurface())
                .orElseGet(() -> vocabRepository.save(vocab));

        if (!video.getVocabs().contains(savedVocab)) {
            video.getVocabs().add(savedVocab);
        }
        savedVocab.getVideos().add(video);
        youtubeVideoRepository.save(video);
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
                .map(vocabMapper::toVocabResponse)
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
