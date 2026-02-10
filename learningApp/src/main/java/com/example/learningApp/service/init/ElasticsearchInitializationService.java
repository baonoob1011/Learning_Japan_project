package com.example.learningApp.service.init;

import com.example.learningApp.service.video.YoutubeVideoSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ElasticsearchInitializationService {

    private final YoutubeVideoSyncService youtubeVideoSyncService;

    public void syncYoutubeVideos() {
        youtubeVideoSyncService.syncIfNotExists();
    }
}
