package com.example.learningApp.service.vocab;

import com.example.learningApp.dto.cache.VocabCache;
import com.example.learningApp.dto.request.vocab.CreateVocabRequest;
import com.example.learningApp.dto.response.translate.TranslateResponse;
import com.example.learningApp.entity.Vocab;
import com.example.learningApp.entity.YoutubeVideo;
import com.example.learningApp.mapper.VocabMapper;
import com.example.learningApp.repository.VocabRepository;
import com.example.learningApp.repository.YoutubeVideoRepository;
import com.example.learningApp.service.translate.TranslateService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
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


    public TranslateResponse findOrTranslate(String videoId, String text, String source, String target) throws IOException {

        // 1️⃣ Lấy vocab cache từ Redis
        String redisKey = "vocabCache:" + videoId;
        List<VocabCache> vocabCacheList = (List<VocabCache>) redisTemplate.opsForValue().get(redisKey);

        if (vocabCacheList != null) {
            // 2️⃣ Tìm vocab trong cache theo surface
            for (VocabCache cache : vocabCacheList) {
                if (cache.getSurface().equalsIgnoreCase(text)) {
                    return new TranslateResponse(
                            videoId,
                            cache.getSurface(),
                            cache.getTranslated(),
                            cache.getReading(),
                            cache.getRomaji(),
                            cache.getPartOfSpeech(),
                            cache.getTargetDefs(),  // thêm nếu VocabCache có
                            cache.getAudioUrl(),
                            cache.getExplain()
                    );
                }
            }
        }

        // 3️⃣ Nếu không có trong cache -> tìm trong DB
        var optionalVocab = vocabRepository.findBySurface(text);
        if (optionalVocab.isPresent()) {
            var vocab = optionalVocab.get();

            // 4️⃣ Optional: cập nhật cache mới
            if (vocabCacheList != null) {
                VocabCache newCache = vocabMapper.toVocabCache(vocab);
                vocabCacheList.add(newCache);
                redisTemplate.opsForValue().set(redisKey, vocabCacheList, Duration.ofHours(1));
            }

            return new TranslateResponse(
                    videoId,
                    vocab.getSurface(),
                    vocab.getTranslated(),
                    vocab.getReading(),
                    vocab.getRomaji(),
                    vocab.getPartOfSpeech(),
                    vocab.getTargetDefs(),
                    vocab.getAudioUrl(),
                    vocab.getExplain()
            );
        }

        // 5️⃣ Nếu không tìm thấy trong DB -> gọi translateService
        return translateService.translate(videoId, text, source, target);
    }

}
