package com.example.learningApp.controller.vocab;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.entity.VocabPracticeQuestion;
import com.example.learningApp.service.vocab.VocabExerciseService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vocab/practice")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VocabExerciseController {

    VocabExerciseService exerciseService;
    EntityFinder finder;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<Void>> generateBatch() {
        var user = finder.userById();
        exerciseService.generateForBatch(user, user.getSavedVocabs().stream().toList());
        return ResponseEntity.ok(ApiResponse.success("Generating batch exercises in background", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VocabPracticeQuestion>>> getMyExercises() {
        var user = finder.userById();
        return ResponseEntity.ok(ApiResponse.success("Fetched exercises successfully", exerciseService.getExercisesForUser(user)));
    }
}
