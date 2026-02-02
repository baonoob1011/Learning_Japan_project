package com.example.learningApp.common;


import com.example.learningApp.entity.User;
import com.example.learningApp.entity.Vocab;
import com.example.learningApp.entity.YoutubeVideo;
import com.example.learningApp.exception.NotFoundException;
import com.example.learningApp.repository.UserRepository;
import com.example.learningApp.repository.VocabRepository;
import com.example.learningApp.repository.YoutubeVideoRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import static com.example.learningApp.utils.RepositoryUtil.findOrThrow;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EntityFinder {

    UserRepository userRepository;
    YoutubeVideoRepository youtubeVideoRepository;
    VocabRepository vocabRepository;

    public User userById() {

        return findOrThrow(
                userRepository,
                SecurityContextHolder.getContext().getAuthentication().getName(),
                () -> new NotFoundException("User not found")
        );
    }
    public Vocab vocabBySurface(String surface) {
        return vocabRepository.findBySurface(surface)
                .orElseThrow(() ->
                        new NotFoundException("Vocab not found: " + surface)
                );
    }

    public YoutubeVideo videoById(String surface) {
        return findOrThrow(
                youtubeVideoRepository,
                surface,
                () -> new NotFoundException("Video not found")
        );
    }
    public Vocab vocabId(String vocabId) {
        return findOrThrow(
                vocabRepository,
                vocabId,
                () -> new NotFoundException("Vocab not found")
        );
    }
}
