package com.example.learningApp.controller.search;


import com.example.learningApp.dto.ApiResponse;
import com.example.learningApp.dto.response.search.SuggestResponse;
import com.example.learningApp.dto.response.video.YoutubeVideoSummaryResponse;
import com.example.learningApp.service.search.YoutubeVideoSearchService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/videos/search")
@RequiredArgsConstructor
public class YoutubeVideoSearchController {

    private final YoutubeVideoSearchService youtubeVideoSearchService;

    // 🔍 SEARCH GỢI Ý (autocomplete)
    @GetMapping
    public ResponseEntity<ApiResponse<List<YoutubeVideoSummaryResponse>>> searchVideos(
            @RequestParam("key") String keyword
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Search videos successfully",
                        youtubeVideoSearchService.searchSuggest(keyword)
                )
        );
    }

}
