package com.example.learningApp.service.exam;

import com.example.learningApp.dto.request.exam.UpdatePassageRequest;
import com.example.learningApp.dto.response.exam.PassageResponse;
import com.example.learningApp.entity.Passage;
import com.example.learningApp.mapper.PassageMapper;
import com.example.learningApp.repository.PassageRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PassageService {

    PassageRepository passageRepository;
    PassageMapper passageMapper;

    @Transactional
    public PassageResponse updatePassage(String passageId, UpdatePassageRequest request) {
        Passage passage = passageRepository.findById(passageId)
                .orElseThrow(() -> new RuntimeException("Passage not found with id: " + passageId));

        passageMapper.updatePassageFromRequest(request, passage);
        Passage updatedPassage = passageRepository.save(passage);

        log.info("Passage updated successfully: {}", passageId);
        return passageMapper.toPassageResponse(updatedPassage);
    }

    public PassageResponse getPassageById(String passageId) {
        Passage passage = passageRepository.findById(passageId)
                .orElseThrow(() -> new RuntimeException("Passage not found with id: " + passageId));
        return passageMapper.toPassageResponse(passage);
    }

    @Transactional
    public void deletePassage(String passageId) {
        Passage passage = passageRepository.findById(passageId)
                .orElseThrow(() -> new RuntimeException("Passage not found with id: " + passageId));
        passageRepository.delete(passage);
        log.info("Passage deleted successfully: {}", passageId);
    }
}

