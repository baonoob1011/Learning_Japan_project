package com.example.learningApp.service.vocab;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.response.vocab.VocabStatusResponse;
import com.example.learningApp.entity.User;
import com.example.learningApp.entity.UserVocabProgress;
import com.example.learningApp.entity.Vocab;
import com.example.learningApp.enums.LearningStatus;
import com.example.learningApp.repository.UserVocabProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VocabStatusService {

    private final UserVocabProgressRepository progressRepo;
    private final EntityFinder finder;

    public VocabStatusResponse getStatus(String vocabId) {

        User user = finder.userById();
        Vocab vocab = finder.vocabId(vocabId);

        return progressRepo.findByUserAndVocab(user, vocab)
                .map(p -> new VocabStatusResponse(
                        vocabId,
                        p.getStatus()
                ))
                // 👇 chưa học lần nào
                .orElse(new VocabStatusResponse(
                        vocabId,
                        LearningStatus.NEW
                ));
    }
}

