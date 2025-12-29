package com.example.learningApp.service.vocab;

import com.example.learningApp.dto.cache.VocabCache;
import com.example.learningApp.dto.request.vocab.CreateVocabRequest;
import com.example.learningApp.dto.response.translate.TranslateResponse;
import com.example.learningApp.entity.Exam;
import com.example.learningApp.entity.User;
import com.example.learningApp.entity.Vocab;
import com.example.learningApp.entity.YoutubeVideo;
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

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class VocabService {
    VocabRepository vocabRepository;
    VocabMapper vocabMapper;
    YoutubeVideoRepository youtubeVideoRepository;
    TranslateService translateService;
    UserRepository userRepository;
     RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public void createVocab(CreateVocabRequest request) {
        Vocab vocab = vocabMapper.toVocab(request);

        YoutubeVideo video = youtubeVideoRepository.findById(request.getVideoId())
                .orElseThrow(() -> new RuntimeException("Video not found"));

        Vocab savedVocab = vocabRepository.findBySurface(vocab.getSurface())
                .orElseGet(() -> vocabRepository.save(vocab));

        if (!video.getVocabs().contains(savedVocab)) {
            video.getVocabs().add(savedVocab);
        }

        savedVocab.getVideos().add(video);

        youtubeVideoRepository.save(video);

        System.out.println("Vocab linked to video successfully: " + savedVocab.getSurface());
    }


    public TranslateResponse findOrTranslate(String videoId, String text, String source, String target) throws IOException, InterruptedException {
        String redisKey = "vocabCache:" + videoId + ":" + text.toLowerCase();
        Object cached = redisTemplate.opsForValue().get(redisKey);
        VocabCache cache = null;

        if (cached != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            // Convert Object -> VocabCache
            cache = objectMapper.convertValue(cached, VocabCache.class);
        }

        if (cache != null) {
            // Nếu tìm thấy trong Redis
            return new TranslateResponse(
                    videoId,
                    cache.getSurface(),
                    cache.getTranslated(),
                    cache.getReading(),
                    cache.getRomaji(),
                    cache.getPartOfSpeech(),
                    cache.getTargetDefs(),
                    cache.getAudioUrl()
            );
        }

        // Nếu không có trong Redis -> tìm trong DB
        var optionalVocab = vocabRepository.findBySurface(text);
        if (optionalVocab.isPresent()) {
            Vocab vocab = optionalVocab.get();

            // Cache vocab mới vào Redis
            VocabCache newCache = new VocabCache(
                    vocab.getId(),
                    vocab.getSurface(),
                    vocab.getRomaji(),
                    vocab.getTranslated(),
                    vocab.getReading(),
                    vocab.getTargetDefs(),
                    vocab.getPartOfSpeech(),
                    vocab.getAudioUrl()
            );
            redisTemplate.opsForValue().set(redisKey, newCache, Duration.ofHours(1));

            return new TranslateResponse(
                    videoId,
                    vocab.getSurface(),
                    vocab.getTranslated(),
                    vocab.getReading(),
                    vocab.getRomaji(),
                    vocab.getPartOfSpeech(),
                    vocab.getTargetDefs(),
                    vocab.getAudioUrl()
            );
        }

        // Nếu không tìm thấy -> gọi TranslateService
        return translateService.translate(videoId, text, source, target);
    }
    public Void saveVocabForCurrentUser(String surface) {

        // 1️⃣ Lấy userId từ sub
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Vocab vocab = vocabRepository.findBySurface(surface)
                .orElseThrow(() -> new RuntimeException("Vocab not found"));

        // 2️⃣ Tránh lưu trùng
        if (user.getSavedVocabs().contains(vocab)) {
            return null;
        }

        // 3️⃣ Lưu quan hệ
        user.getSavedVocabs().add(vocab);
        userRepository.save(user);

        return null;
    }


}
