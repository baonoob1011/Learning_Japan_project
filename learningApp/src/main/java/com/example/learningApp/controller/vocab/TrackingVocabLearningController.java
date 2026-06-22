package com.example.learningApp.controller.vocab;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.vocab.MarkVocabRequest;
import com.example.learningApp.dto.request.vocab.SmartFinalizeWordRequest;
import com.example.learningApp.dto.request.vocab.SmartSkillAttemptRequest;
import com.example.learningApp.dto.response.vocab.SmartFinalizeWordResponse;
import com.example.learningApp.dto.response.vocab.UserVocabProgressResponse;
import com.example.learningApp.service.vocab.TrackingVocabLearningService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/learning")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TrackingVocabLearningController {

        TrackingVocabLearningService learningService;

        /**
         * ✅ User đánh dấu từ vựng: thuộc / chưa thuộc
         * UI: nút "Chưa thuộc" / "Thuộc"
         */
        @PostMapping("/vocab/mark")
        public ResponseEntity<ApiResponse<Void>> markVocab(
                        @RequestBody MarkVocabRequest request) {
                learningService.markVocab(request);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Marked vocab learning status successfully",
                                                null));
        }

        @GetMapping("/vocab/progress")
        public ResponseEntity<ApiResponse<List<UserVocabProgressResponse>>> getMyProgress() {

                List<UserVocabProgressResponse> progressList = learningService.getMyLearningProgress();

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Fetched vocab learning progress successfully",
                                                progressList));
        }

        @PostMapping("/smart/attempt-skill")
        public ResponseEntity<ApiResponse<Void>> recordSmartSkillAttempt(
                        @RequestBody SmartSkillAttemptRequest request) {
                learningService.recordSmartSkillAttempt(
                                request.getVocabId(),
                                request.getSkill(),
                                request.getStudyMode(),
                                request.isSuccess());

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Recorded smart-study skill attempt",
                                                null));
        }

        @PostMapping("/smart/finalize-word")
        public ResponseEntity<ApiResponse<SmartFinalizeWordResponse>> finalizeSmartWord(
                        @RequestBody SmartFinalizeWordRequest request) {
                SmartFinalizeWordResponse response = learningService.finalizeSmartWord(
                                request.getVocabId(),
                                request.getWrongCount(),
                                request.isFailedInRetry());

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Finalized smart-study word",
                                                response));
        }
}
