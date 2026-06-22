// AdminServiceTest.java
package com.example.learningApp.service.admin;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.request.exam.CreateExamRequest;
import com.example.learningApp.dto.request.feedback.AdminUpdateFeedbackRequest;
import com.example.learningApp.dto.request.kanji.CreateKanjiRequest;
import com.example.learningApp.dto.response.exam.ExamResponse;
import com.example.learningApp.dto.response.feedback.FeedbackResponse;
import com.example.learningApp.dto.response.kanji.KanjiAiResponse;
import com.example.learningApp.dto.response.kanji.KanjiResponse;
import com.example.learningApp.dto.response.user.UserResponse;
import com.example.learningApp.entity.Exam;
import com.example.learningApp.entity.Feedback;
import com.example.learningApp.entity.Kanji;
import com.example.learningApp.entity.User;
import com.example.learningApp.entity.YoutubeVideo;
import com.example.learningApp.enums.FeedbackStatus;
import com.example.learningApp.enums.FeedbackType;
import com.example.learningApp.enums.JLPTLevel;
import com.example.learningApp.enums.VideoTag;
import com.example.learningApp.mapper.ExamMapper;
import com.example.learningApp.mapper.FeedbackMapper;
import com.example.learningApp.mapper.OrderMapper;
import com.example.learningApp.mapper.UserMapper;
import com.example.learningApp.repository.ExamRepository;
import com.example.learningApp.repository.FeedbackRepository;
import com.example.learningApp.repository.KanjiRepository;
import com.example.learningApp.repository.UserLearningProgressRepository;
import com.example.learningApp.repository.UserRepository;
import com.example.learningApp.repository.UserVocabProgressRepository;
import com.example.learningApp.repository.VocabRepository;
import com.example.learningApp.repository.VipPackageRepository;
import com.example.learningApp.repository.YoutubeVideoRepository;
import com.example.learningApp.service.ai.KanjiAiService;
import com.example.learningApp.service.ai.KanjiStrokeAiService;
import com.example.learningApp.service.exam.ExamService;
import com.example.learningApp.service.feeback.FeedbackService;
import com.example.learningApp.service.kanji.KanjiService;
import com.example.learningApp.service.user.UserService;
import com.example.learningApp.service.video.YoutubeVideoInfoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private YoutubeVideoRepository youtubeVideoRepository;
    @Mock private ExamRepository examRepository;
    @Mock private ExamMapper examMapper;
    @Mock private KanjiRepository kanjiRepository;
    @Mock private KanjiAiService kanjiAiService;
    @Mock private KanjiStrokeAiService kanjiStrokeAiService;
    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private CognitoIdentityProviderClient cognitoClient;
    @Mock private FeedbackRepository feedbackRepository;
    @Mock private FeedbackMapper feedbackMapper;
    @Mock private EntityFinder entityFinder;
    @Mock private com.example.learningApp.service.role.RoleService roleService;
    @Mock private com.example.learningApp.service.chat.ChatRoomCommandService chatRoomCommandService;
    @Mock private com.example.learningApp.service.auth.SessionService sessionService;
    @Mock private UserLearningProgressRepository userLearningProgressRepository;
    @Mock private UserVocabProgressRepository userVocabProgressRepository;
    @Mock private com.example.learningApp.repository.OrderRepository orderRepository;
    @Mock private OrderMapper orderMapper;
    @Mock private VocabRepository vocabRepository;
    @Mock private VipPackageRepository vipPackageRepository;
    @Spy private ObjectMapper objectMapper = new ObjectMapper();

    @org.mockito.InjectMocks
    private YoutubeVideoInfoService youtubeVideoInfoService;
    @org.mockito.InjectMocks
    private ExamService examService;
    @org.mockito.InjectMocks
    private KanjiService kanjiService;
    @org.mockito.InjectMocks
    private UserService userService;
    @org.mockito.InjectMocks
    private FeedbackService feedbackService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(youtubeVideoInfoService, "youtubeApiKey", "api-key");
        ReflectionTestUtils.setField(userService, "clientId", "client-id");
        ReflectionTestUtils.setField(userService, "clientSecret", "client-secret");
        ReflectionTestUtils.setField(userService, "userPoolId", "user-pool-id");
    }

    @Test
    @DisplayName("TC31 - Admin uploads a new video and it is stored with metadata")
    void tc31_upload_video_new() throws Exception {
        // Given
        String youtubeUrl = "https://www.youtube.com/watch?v=video123";
        String apiJson = """
                {"items":[{"snippet":{"title":"Video title","description":"Video desc","channelTitle":"Channel","thumbnails":{"high":{"url":"thumb.jpg"}},"publishedAt":"2026-03-29T00:00:00Z"},"contentDetails":{"duration":"PT10M"}}]}
                """;

        try (MockedStatic<HttpClient> httpClientStatic = mockStatic(HttpClient.class)) {
            HttpClient httpClient = mock(HttpClient.class);
            HttpResponse<String> httpResponse = mock(HttpResponse.class);
            httpClientStatic.when(HttpClient::newHttpClient).thenReturn(httpClient);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
            when(httpResponse.body()).thenReturn(apiJson);
            when(youtubeVideoRepository.findById("video123")).thenReturn(Optional.empty());
            when(youtubeVideoRepository.save(any(YoutubeVideo.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            YoutubeVideo video = youtubeVideoInfoService.fetchAndSaveVideoInfo(youtubeUrl, "video123");

            // Then
            assertEquals("video123", video.getId());
            assertEquals("Video title", video.getTitle());
            assertEquals("PT10M", video.getDuration());
            assertEquals(youtubeUrl, video.getUrlVideo());
            verify(youtubeVideoRepository).save(any(YoutubeVideo.class));
        }
    }

    @Test
    @DisplayName("TC32 - Admin imports an exam from Excel into the system")
    void tc32_import_exam_from_excel() {
        // Given
        CreateExamRequest request = CreateExamRequest.builder()
                .code("N3-2026")
                .level("N3")
                .duration(60)
                .numSections(2)
                .numQuestions(100)
                .build();
        Exam exam = Exam.builder().id("exam-1").code("N3-2026").level("N3").duration(60).numSections(2).numQuestions(100).build();
        ExamResponse expected = ExamResponse.builder().id("exam-1").code("N3-2026").level("N3").duration(60).numQuestions(100).build();
        when(examRepository.existsByCode("N3-2026")).thenReturn(false);
        when(examMapper.toExam(request)).thenReturn(exam);
        when(examRepository.save(any(Exam.class))).thenReturn(exam);
        when(examMapper.toExamResponse(exam)).thenReturn(expected);

        // When
        ExamResponse response = examService.createExam(request);

        // Then
        assertEquals("N3-2026", response.getCode());
        assertEquals(100, response.getNumQuestions());
        verify(examRepository).save(exam);
    }

    @Test
    @DisplayName("TC33 - Admin adds Kanji and AI generates writing animation")
    void tc33_add_kanji_and_ai_animation() {
        // Given
        CreateKanjiRequest request = new CreateKanjiRequest();
        request.setCharacter("水");
        KanjiAiResponse aiResponse = new KanjiAiResponse();
        aiResponse.setMeaning("water");
        aiResponse.setOnyomi("スイ");
        aiResponse.setKunyomi("みず");
        when(kanjiAiService.generateKanjiData("水")).thenReturn(aiResponse);
        when(kanjiStrokeAiService.generateSvgStrokes("水")).thenReturn(List.of("a", "b"));
        when(kanjiRepository.save(any(Kanji.class))).thenAnswer(invocation -> {
            Kanji kanji = invocation.getArgument(0);
            kanji.setId("k1");
            return kanji;
        });

        // When
        KanjiResponse response = kanjiService.createKanji(request);

        // Then
        assertEquals("k1", response.getId());
        assertEquals("水", response.getCharacter());
        assertEquals(List.of("a", "b"), response.getSvgStrokes());
    }

    @Test
    @DisplayName("TC34 - Admin disables a violating user account")
    void tc34_ban_user_account() {
        // Given
        User user = User.builder().id("u1").email("violator@example.com").enabled(true).build();
        when(userRepository.findByEmail("violator@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        userService.banUser("violator@example.com");

        // Then
        assertFalse(Boolean.TRUE.equals(user.getEnabled()));
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("TC35 - Admin replies to feedback and user sees the response")
    void tc35_reply_feedback() {
        // Given
        User user = User.builder().id("u1").email("user@example.com").build();
        Feedback feedback = Feedback.builder()
                .id("f1")
                .user(user)
                .type(FeedbackType.BUG)
                .content("Bug report")
                .status(FeedbackStatus.PENDING)
                .build();
        AdminUpdateFeedbackRequest request = new AdminUpdateFeedbackRequest();
        request.setStatus(FeedbackStatus.RESOLVED);
        request.setAdminReply("Fixed in the next release");
        FeedbackResponse expected = FeedbackResponse.builder()
                .id("f1")
                .userId("u1")
                .type(FeedbackType.BUG)
                .content("Bug report")
                .status(FeedbackStatus.RESOLVED)
                .adminReply("Fixed in the next release")
                .build();

        when(entityFinder.findFeedbackById("f1")).thenReturn(feedback);
        when(feedbackRepository.save(any(Feedback.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(feedbackMapper.toResponse(any(Feedback.class))).thenReturn(expected);

        // When
        FeedbackResponse response = feedbackService.updateFeedback("f1", request);

        // Then
        assertEquals(FeedbackStatus.RESOLVED, response.getStatus());
        assertEquals("Fixed in the next release", response.getAdminReply());
        verify(feedbackRepository).save(feedback);
    }
}
