package com.example.learningApp.service.vocab;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.cache.VocabCache;
import com.example.learningApp.dto.request.translate.TranslateRequest;
import com.example.learningApp.dto.request.vocab.CreateVocabRequest;
import com.example.learningApp.dto.request.vocab.UpdateVocabRequest;
import com.example.learningApp.dto.response.translate.TranslateResponse;
import com.example.learningApp.dto.response.vocab.VocabResponse;
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

import static java.awt.SystemColor.text;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class VocabService {
    VocabRepository vocabRepository;
    VocabMapper vocabMapper;
    EntityFinder finder;
    YoutubeVideoRepository youtubeVideoRepository;
    TranslateService translateService;
    UserRepository userRepository;
     RedisTemplate<String, Object> redisTemplate;

    public List<VocabResponse> getSavedVocabsOfCurrentUserByVideo(String videoId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        List<Vocab> vocabs =
                vocabRepository.findSavedVocabsByUserAndVideo(userId, videoId);

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

        System.out.println("Vocab linked to video successfully: " + savedVocab.getSurface());
    }


    public TranslateResponse findOrTranslate(TranslateRequest request) throws IOException, InterruptedException {
        String redisKey = "vocabCache:" + request.getVideoId() + ":" + request.getText().toLowerCase();
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
                    request.getVideoId(),
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
        var optionalVocab = vocabRepository.findBySurface(request.getText());
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
                    request.getVideoId(),
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
        return translateService.translate(request);
    }
    public Void saveVocabForCurrentUser(String surface) {

        var user = finder.userById();


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
        Vocab vocab = vocabRepository.findBySurface(surface)
                .orElseThrow(() -> new RuntimeException("Vocab not found"));

        // 3️⃣ Xóa quan hệ
        if (user.getSavedVocabs().remove(vocab)) {
            vocab.getUsers().remove(user); // ✔ giữ consistency 2 chiều
            userRepository.save(user);
        }
    }
    @Transactional
    public void updateVocabMeaning(UpdateVocabRequest request) {

        Vocab vocab = vocabRepository.findBySurface(request.getSurface())
                .orElseThrow(() -> new RuntimeException("Vocab not found"));

        // 🔒 CHỈ sửa nghĩa
        vocab.setTranslated(request.getTranslated());

        vocabRepository.save(vocab);
    }

}
