package com.example.learningApp.service.progress;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.response.progress.SkillProgressResponse;
import com.example.learningApp.enums.SkillCategory;
import com.example.learningApp.service.progress.core.SkillProgressReader;
import com.example.learningApp.service.progress.core.SkillProgressUpdater;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProgressTrackingService {

    private final SkillProgressUpdater updater;
    private final SkillProgressReader reader;
    private final EntityFinder finder;

    @Transactional
    public void updateSkillProgress(
            String userId,
            Map<SkillCategory, Integer> totalMap,
            Map<SkillCategory, Integer> correctMap
    ) {
        for (SkillCategory category : SkillCategory.values()) {
            updater.update(
                    userId,
                    category,
                    totalMap.getOrDefault(category, 0),
                    correctMap.getOrDefault(category, 0)
            );
        }
    }

    public SkillProgressResponse getSkillProgress( ) {

        var user=finder.userById();
        return SkillProgressResponse.builder()
                .vocabulary(reader.getAccuracy(user.getId(), SkillCategory.VOCABULARY))
                .grammar(reader.getAccuracy(user.getId(), SkillCategory.GRAMMAR))
                .reading(reader.getAccuracy(user.getId(), SkillCategory.READING))
                .listening(reader.getAccuracy(user.getId(), SkillCategory.LISTENING))
                .kanji(reader.getAccuracy(user.getId(), SkillCategory.KANJI))
                .build();
    }
}
