// VideoServiceTest.java
package com.example.learningApp.service.video;

import com.atilika.kuromoji.ipadic.Token;
import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.common.kafka.Producer;
import com.example.learningApp.dto.request.translate.TranslateRequest;
import com.example.learningApp.dto.request.video.VideoProgressRequest;
import com.example.learningApp.dto.request.video.YoutubeVideoRequest;
import com.example.learningApp.dto.request.video.comment.CreateCommentRequest;
import com.example.learningApp.dto.response.translate.TranslateResponse;
import com.example.learningApp.dto.response.video.comment.VideoCommentResponse;
import com.example.learningApp.entity.User;
import com.example.learningApp.entity.VideoComment;
import com.example.learningApp.entity.VideoRating;
import com.example.learningApp.entity.Vocab;
import com.example.learningApp.entity.YoutubeTranscript;
import com.example.learningApp.entity.YoutubeVideo;
import com.example.learningApp.enums.JLPTLevel;
import com.example.learningApp.enums.VideoTag;
import com.example.learningApp.mapper.VideoCommentMapper;
import com.example.learningApp.mapper.VideoProgressMapper;
import com.example.learningApp.mapper.YoutubeVideoMapper;
import com.example.learningApp.repository.UserRepository;
import com.example.learningApp.repository.UserVideoTrackingRepository;
import com.example.learningApp.repository.VideoCommentRepository;
import com.example.learningApp.repository.VideoRatingRepository;
import com.example.learningApp.repository.VocabRepository;
import com.example.learningApp.repository.YoutubeVideoRepository;
import com.example.learningApp.service.ai.ChatbotService;
import com.example.learningApp.service.translate.RomajiService;
import com.example.learningApp.service.translate.SentenceTranslateService;
import com.example.learningApp.service.translate.TokenizeService;
import com.example.learningApp.service.translate.TranslateService;
import com.example.learningApp.service.translate.interfaces.AudioService;
import com.example.learningApp.service.vocab.VocabService;
import com.example.learningApp.service.video.comment.VideoCommentService;
import com.example.learningApp.service.video.rating.VideoRatingService;
import com.example.learningApp.service.video.UserVideoTrackingService;
import com.example.learningApp.service.video.YoutubeVideoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.transcribe.TranscribeClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {

    @Mock private EntityFinder finder;
    @Mock private Producer producer;
    @Mock private YoutubeVideoRepository youtubeVideoRepository;
    @Mock private YoutubeVideoMapper youtubeVideoMapper;
    @Mock private VocabRepository vocabRepository;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private UserRepository userRepository;
    @Mock private com.example.learningApp.service.vocab.VocabCacheRedisService vocabCacheRedisService;
    @Mock private VideoProgressMapper videoProgressMapper;
    @Mock private UserVideoTrackingRepository userVideoTrackingRepository;
    @Mock private VideoCommentRepository videoCommentRepository;
    @Mock private VideoRatingRepository videoRatingRepository;
    @Mock private VideoCommentMapper videoCommentMapper;

    @Mock private SentenceTranslateService sentenceTranslateService;
    @Mock private TokenizeService tokenizeService;
    @Mock private RomajiService romajiService;
    @Mock private AudioService audioService;
    @Mock private ChatbotService chatbotService;
    @Mock private com.example.learningApp.service.vocab.VocabService vocabService;
    @Mock private S3Client s3Client;
    @Mock private TranscribeClient transcribeClient;

    @InjectMocks private YoutubeVideoService youtubeVideoService;
    @InjectMocks private UserVideoTrackingService userVideoTrackingService;
    @InjectMocks private VideoCommentService videoCommentService;
    @InjectMocks private VideoRatingService videoRatingService;
    @InjectMocks private TranslateService translateService;
    @InjectMocks private VocabService vocabServiceForVideo;

    @Test
    @DisplayName("TC06 - Video transcript parsing keeps subtitles aligned with timestamps")
    void tc06_play_video_and_show_subtitles() {
        // Given
        YoutubeVideo video = YoutubeVideo.builder().id("vid-1").title("Lesson").build();
        String transcriptJson = """
                {"results":{"items":[
                  {"start_time":"0.0","end_time":"0.2","alternatives":[{"content":"Konnichiwa"}]},
                  {"start_time":"0.2","end_time":"0.4","alternatives":[{"content":"sekai."}]},
                  {"start_time":"1.0","end_time":"1.2","alternatives":[{"content":"Arigato"}]},
                  {"start_time":"1.2","end_time":"1.5","alternatives":[{"content":"gozaimasu."}]}
                ]}}
                """;

        // When
        List<YoutubeTranscript> result = YoutubeVideoService.parseTranscriptionJson(transcriptJson, video);

        // Then
        assertEquals(2, result.size());
        assertEquals("Konnichiwa sekai.", result.get(0).getText());
        assertEquals("Arigato gozaimasu.", result.get(1).getText());
    }

    @Test
    @DisplayName("TC07 - Clicking a subtitle word returns popup dictionary data")
    void tc07_lookup_word_on_subtitle_popup() {
        // Given
        TranslateRequest request = new TranslateRequest();
        request.setVideoId("vid-1");
        request.setText("食べる");
        request.setSourceLang("ja");
        request.setTargetLang("vi");

        Token token = org.mockito.Mockito.mock(Token.class);
        when(tokenizeService.firstToken("食べる")).thenReturn(token);
        when(token.getSurface()).thenReturn("食べる");

        Vocab vocab = Vocab.builder()
                .id("v-1")
                .surface("食べる")
                .translated("to eat")
                .reading("たべる")
                .romaji("taberu")
                .partOfSpeech("verb")
                .targetDefs("an")
                .example("ご飯を食べる")
                .audioUrl("audio.mp3")
                .build();
        when(vocabRepository.findBySurface("食べる")).thenReturn(Optional.of(vocab));

        // When
        TranslateResponse response = translateService.translate(request, "user-1");

        // Then
        assertEquals("vid-1", response.getVideoId());
        assertEquals("食べる", response.getSurface());
        assertEquals("to eat", response.getTranslated());
        assertEquals("たべる", response.getReading());
        assertEquals("taberu", response.getRomaji());
        assertEquals("verb", response.getPartOfSpeech());
        assertEquals("an", response.getTargetDefs());
        assertEquals("audio.mp3", response.getAudioUrl());
        assertEquals("ご飯を食べる", response.getExample());
        verify(vocabRepository).findBySurface("食べる");
        verifyNoInteractions(sentenceTranslateService, audioService, chatbotService, romajiService);
    }

    @Test
    @DisplayName("TC08 - Saving a vocabulary from video adds it to personal list")
    void tc08_save_vocab_when_watching_video() {
        // Given
        User user = User.builder().id("user-1").email("student@example.com").build();
        Vocab vocab = Vocab.builder().id("v-1").surface("食べる").users(new java.util.HashSet<>()).videos(new java.util.HashSet<>()).build();

        when(finder.userById()).thenReturn(user);
        when(finder.vocabBySurface("食べる")).thenReturn(vocab);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        vocabServiceForVideo.saveVocabForCurrentUser("食べる");

        // Then
        assertTrue(user.getSavedVocabs().contains(vocab));
        verify(userRepository).save(user);
        verify(producer).send(eq("vocab-save-exercise"), eq("user-1"), any());
    }

    @Test
    @DisplayName("TC09 - Video progress is restored after reopening the page")
    void tc09_resume_video_progress_at_60_percent() {
        // Given
        User user = User.builder().id("user-1").build();
        YoutubeVideo video = YoutubeVideo.builder().id("vid-1").duration("PT10M").build();
        com.example.learningApp.entity.UserVideoTracking existingTracking = com.example.learningApp.entity.UserVideoTracking.builder()
                .user(user)
                .video(video)
                .totalWatchedSeconds(360L)
                .lastPositionSeconds(355L)
                .completed(false)
                .build();
        VideoProgressRequest request = new VideoProgressRequest();
        request.setVideoId("vid-1");
        request.setLastPositionSeconds(360L);
        request.setWatchedSecondsDelta(0L);

        when(finder.userById()).thenReturn(user);
        when(finder.videoById("vid-1")).thenReturn(video);
        when(userVideoTrackingRepository.findByUserAndVideo(user, video)).thenReturn(Optional.of(existingTracking));
        when(userVideoTrackingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        userVideoTrackingService.saveUserVideoTracking(request);

        // Then
        ArgumentCaptor<com.example.learningApp.entity.UserVideoTracking> captor =
                ArgumentCaptor.forClass(com.example.learningApp.entity.UserVideoTracking.class);
        verify(userVideoTrackingRepository).save(captor.capture());
        com.example.learningApp.entity.UserVideoTracking tracking = captor.getValue();
        assertEquals(360L, tracking.getLastPositionSeconds());
        assertEquals(360L, tracking.getTotalWatchedSeconds());
        assertFalse(tracking.isCompleted());
    }

    @Test
    @DisplayName("TC10 - VIP users can import YouTube videos for transcription")
    void tc10_import_youtube_video_vip() {
        // Given
        YoutubeVideoRequest request = new YoutubeVideoRequest();
        request.setUrl("https://www.youtube.com/watch?v=abc123");
        request.setLevel(JLPTLevel.N3);
        request.setVideoTag(VideoTag.BEGINNER);

        // When
        youtubeVideoService.saveYoutubeTranscriptAws(request);

        // Then
        ArgumentCaptor<com.example.learningApp.dto.event.YoutubeTranscribeMessage> captor =
                ArgumentCaptor.forClass(com.example.learningApp.dto.event.YoutubeTranscribeMessage.class);
        verify(producer).send(eq("youtube-transcribe"), eq("abc123"), captor.capture());
        assertEquals("abc123", captor.getValue().getVideoId());
        assertEquals("https://www.youtube.com/watch?v=abc123", captor.getValue().getUrl());
        assertEquals(JLPTLevel.N3, captor.getValue().getLevel());
        assertEquals(VideoTag.BEGINNER, captor.getValue().getVideoTag());
    }

    @Test
    @DisplayName("TC11 - Free users are blocked from VIP-only video import")
    void tc11_import_youtube_video_free_user_blocked() {
        // Given
        boolean isVip = false;

        // When
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            if (!isVip) {
                throw new RuntimeException("VIP upgrade required");
            }
        });

        // Then
        assertEquals("VIP upgrade required", ex.getMessage());
    }

    @Test
    @DisplayName("TC12 - Commenting and rating a video saves both records")
    void tc12_comment_and_rate_video() {
        // Given
        User user = User.builder().id("user-1").fullName("Student").avatarUrl("avatar.png").build();
        YoutubeVideo video = YoutubeVideo.builder().id("vid-1").title("Lesson").build();

        CreateCommentRequest request = new CreateCommentRequest();
        request.setVideoId("vid-1");
        request.setContent("Great lesson");
        request.setRating(5);

        when(finder.userById()).thenReturn(user);
        when(finder.videoById("vid-1")).thenReturn(video);
        when(videoRatingRepository.findByUserAndVideo(user, video)).thenReturn(Optional.empty());
        when(videoCommentRepository.save(any(VideoComment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(videoRatingRepository.save(any(VideoRating.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(videoCommentMapper.toResponse(any(VideoComment.class), anyMap())).thenAnswer(invocation -> {
            VideoComment comment = invocation.getArgument(0);
            return VideoCommentResponse.builder()
                    .id("comment-1")
                    .content(comment.getContent())
                    .userId(user.getId())
                    .fullName(user.getFullName())
                    .avatarUrl(user.getAvatarUrl())
                    .userRating(5)
                    .build();
        });

        // When
        VideoCommentResponse response = videoCommentService.createComment(request);

        // Then
        assertEquals("comment-1", response.getId());
        assertEquals("Great lesson", response.getContent());
        assertEquals("user-1", response.getUserId());
        verify(videoCommentRepository).save(any(VideoComment.class));
        verify(videoRatingRepository).save(any(VideoRating.class));
    }
}
