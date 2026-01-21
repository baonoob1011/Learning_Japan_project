package com.example.learningApp.controller.prgress;

import com.example.learningApp.dto.ApiResponse;
import com.example.learningApp.dto.request.vocab.StudyVocabRequest;
import com.example.learningApp.service.progress.VocabLearningService;
import com.example.learningApp.service.vocab.VocabService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vocab-progress")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VocabLearningController {
    VocabLearningService vocabLearningService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> studyVocab(StudyVocabRequest request){
        vocabLearningService.studyVocab(request);
        return ResponseEntity.ok(ApiResponse.success("Study vocab success",null));
    }
}
