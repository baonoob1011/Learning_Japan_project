package com.example.learningApp.controller.flashcard;

import com.example.learningApp.dto.ApiResponse;
import com.example.learningApp.dto.request.flashcard.CreateFlashcardRequest;
import com.example.learningApp.dto.request.flashcard.ReviewFlashcardRequest;
import com.example.learningApp.dto.response.flashcard.FlashcardResponse;
import com.example.learningApp.entity.UserFlashcard;
import com.example.learningApp.service.flashcard.UserFlashcardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/flashcards")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserFlashcardController {

    UserFlashcardService userFlashcardService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createFlashcard(@RequestBody CreateFlashcardRequest request) {
        // Implementation goes here
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        try{
            var newCard = userFlashcardService.createFlashcard(
                    userId,
                    request.getVocabId(),
                    request.getSourceSentence(),
                    request.getVideoId()
            );
            return ResponseEntity.ok(ApiResponse.success("Flashcard created successfully", newCard));
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    //lay danh sach on tap hom nay
    @GetMapping("study-session")
    public ResponseEntity<ApiResponse<List<FlashcardResponse>>> getFlashcardsInStudySession(){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        List<UserFlashcard> flashcards =
                userFlashcardService.getCardsDueForReview(userId);

        List<FlashcardResponse> response =
                flashcards.stream().map(
                        card -> FlashcardResponse.builder()
                                .id(card.getId())
                                .word(card.getVocabulary().getWord())
                                .hiragana(card.getVocabulary().getHiragana())
                                .meaning(card.getVocabulary().getMeaning())
                                .sourceSentence(card.getSourceSentence())
                                .urlVideo(card.getVideo().getUrlVideo())
                                .videoId(card.getVideo().getId())
                                .build()
                ).toList();

        return ResponseEntity.ok(ApiResponse.success("Flashcards retrieved successfully", response));
    }

    //gui ket qua review flashcard
    @PostMapping("/review/{cardId}")
    public ResponseEntity<ApiResponse<?>> reviewFlashcard(@PathVariable String cardId, @RequestBody ReviewFlashcardRequest reviewFlashcardRequest){
        try{
            userFlashcardService.reviewCard(cardId, reviewFlashcardRequest.getRating());
            return ResponseEntity.ok(ApiResponse.success("Flashcard reviewed successfully", null));
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }
}
