// ExamServiceTest.java
package com.example.learningApp.service.exam;

import com.example.learningApp.common.kafka.Producer;
import com.example.learningApp.dto.cache.QuestionCache;
import com.example.learningApp.dto.cache.SectionCache;
import com.example.learningApp.dto.request.exam.StartExamRequest;
import com.example.learningApp.dto.request.exam.SubmitExamRequest;
import com.example.learningApp.dto.response.exam.StartExamResponse;
import com.example.learningApp.dto.response.exam.SubmitExamResponse;
import com.example.learningApp.dto.response.progress.AdminUserProgressResponse;
import com.example.learningApp.dto.response.progress.UserLearningDashboardResponse;
import com.example.learningApp.dto.response.user.UserResponse;
import com.example.learningApp.entity.Exam;
import com.example.learningApp.entity.ExamParticipant;
import com.example.learningApp.entity.ExamSection;
import com.example.learningApp.entity.Question;
import com.example.learningApp.entity.User;
import com.example.learningApp.entity.UserLearningProgress;
import com.example.learningApp.enums.AssessmentType;
import com.example.learningApp.enums.SkillCategory;
import com.example.learningApp.mapper.UserLearningProgressMapper;
import com.example.learningApp.mapper.UserMapper;
import com.example.learningApp.repository.ExamParticipantRepository;
import com.example.learningApp.repository.ExamRepository;
import com.example.learningApp.repository.ExamSectionRepository;
import com.example.learningApp.repository.QuestionRepository;
import com.example.learningApp.repository.UserAnswerRepository;
import com.example.learningApp.repository.UserExamResultRepository;
import com.example.learningApp.repository.UserLearningProgressRepository;
import com.example.learningApp.repository.UserRepository;
import com.example.learningApp.service.progress.AdminLearningService;
import com.example.learningApp.service.progress.ProgressTrackingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamServiceTest {

    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ExamRepository examRepository;
    @Mock private QuestionRepository questionRepository;
    @Mock private ExamSectionRepository examSectionRepository;
    @Mock private ExamParticipantRepository examParticipantRepository;
    @Mock private UserExamResultRepository userExamResultRepository;
    @Mock private UserAnswerRepository userAnswerRepository;
    @Mock private com.example.learningApp.mapper.ExamMapper examMapper;
    @Mock private ExamCacheService examCacheService;
    @Mock private UserRepository userRepository;
    @Mock private Producer producer;
    @Mock private ProgressTrackingService progressTrackingService;
    @Mock private UserLearningProgressRepository userLearningProgressRepository;
    @Mock private UserMapper userMapper;
    @Mock private UserLearningProgressMapper userLearningProgressMapper;

    private ExamParticipantService examParticipantService;
    private ExamSubmitService examSubmitService;
    private AdminLearningService adminLearningService;
    private com.example.learningApp.service.progress.UserLearningProgressService userLearningProgressService;
    private ExamScoringService examScoringService;

    private ValueOperations<String, Object> valueOperations;

    @BeforeEach
    void setUp() {
        examScoringService = new ExamScoringService();
        examParticipantService = new ExamParticipantService(examParticipantRepository, examRepository, userRepository, redisTemplate);
        examSubmitService = new ExamSubmitService(examParticipantRepository, examCacheService, examScoringService, progressTrackingService, producer);
        adminLearningService = new AdminLearningService(userLearningProgressRepository, userRepository, userMapper);
        userLearningProgressService = new com.example.learningApp.service.progress.UserLearningProgressService(userLearningProgressRepository, userRepository, userLearningProgressMapper);
        valueOperations = mock(ValueOperations.class);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("TC17 - Starting an exam opens the timer and caches the paper")
    void tc17_start_exam() {
        // Given
        User user = User.builder().id("u1").email("student@example.com").roles(new java.util.HashSet<>()).build();
        ExamSection section = ExamSection.builder()
                .id("s1")
                .title("Grammar")
                .sectionOrder(1)
                .sectionDuration(30)
                .level("N3")
                .assessmentItems(new LinkedHashSet<>())
                .questions(new LinkedHashSet<>())
                .build();
        Question question = Question.builder()
                .id("q1")
                .section(section)
                .questionType(AssessmentType.FILL_BLANK)
                .questionText("Test question")
                .options(List.of("A", "B"))
                .answer("A")
                .questionOrder(1)
                .build();
        section.getQuestions().add(question);
        Exam exam = Exam.builder()
                .id("exam-1")
                .code("JLPT-N3")
                .level("N3")
                .duration(60)
                .participant(0L)
                .sections(new LinkedHashSet<>(Set.of(section)))
                .questions(new LinkedHashSet<>(Set.of(question)))
                .build();
        StartExamRequest request = StartExamRequest.builder().examId("exam-1").build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("u1", null, List.of()));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(examRepository.findById("exam-1")).thenReturn(Optional.of(exam));
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(examParticipantRepository.countByUser_IdAndStartedAtBetween(eq("u1"), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(0L);
        when(examParticipantRepository.save(any(ExamParticipant.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(examRepository.save(any(Exam.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(valueOperations.get("exam:exam-1:questions")).thenReturn(null);
        when(valueOperations.get("exam:exam-1:sections")).thenReturn(null);

        // When
        StartExamResponse response = examParticipantService.startExam(request);

        // Then
        assertEquals("exam-1", response.getExamId());
        assertEquals("JLPT-N3", response.getExamCode());
        assertFalse(Boolean.TRUE.equals(response.getCompleted()));
        verify(valueOperations, times(2)).set(anyString(), any(), any());
        verify(examParticipantRepository).save(any(ExamParticipant.class));
    }

    @Test
    @DisplayName("TC18 - Submitting an exam returns detailed grading results")
    void tc18_submit_exam_and_view_result() {
        // Given
        User user = User.builder().id("u1").build();
        Exam exam = Exam.builder().id("exam-1").code("N3-01").build();
        ExamParticipant participant = ExamParticipant.builder()
                .id("p1")
                .exam(exam)
                .user(user)
                .completed(false)
                .startedAt(LocalDateTime.now().minusMinutes(30))
                .build();

        QuestionCache question = QuestionCache.builder()
                .id("q1")
                .sectionId("s1")
                .questionType(AssessmentType.FILL_BLANK)
                .questionText("Question")
                .correctAnswer("A")
                .questionOrder(1)
                .build();
        SectionCache section = SectionCache.builder()
                .id("s1")
                .title("Section")
                .sectionOrder(1)
                .sectionDuration(30)
                .pointMap(Map.of(AssessmentType.FILL_BLANK, 5f))
                .build();
        Map<String, QuestionCache> questions = Map.of("q1", question);
        Map<String, SectionCache> sections = Map.of("s1", section);
        SubmitExamRequest request = SubmitExamRequest.builder()
                .participantId("p1")
                .answers(List.of(SubmitExamRequest.AnswerDto.builder().questionId("q1").answer("A").build()))
                .build();

        when(examParticipantRepository.findById("p1")).thenReturn(Optional.of(participant));
        when(examCacheService.getQuestions("exam-1")).thenReturn(questions);
        when(examCacheService.getSections("exam-1")).thenReturn(sections);
        when(examParticipantRepository.save(any(ExamParticipant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        SubmitExamResponse response = examSubmitService.submitExam(request);

        // Then
        assertEquals("p1", response.getParticipantId());
        assertEquals("exam-1", response.getExamId());
        assertEquals(5f, response.getTotalScore());
        assertTrue(response.getCompleted());
        verify(progressTrackingService).updateSkillProgress(eq("u1"), anyMap(), anyMap());
        verify(producer, times(2)).send(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("TC19 - Time-up submission still produces a valid result")
    void tc19_auto_submit_when_time_runs_out() {
        // Given
        User user = User.builder().id("u1").build();
        Exam exam = Exam.builder().id("exam-1").code("N3-01").build();
        ExamParticipant participant = ExamParticipant.builder()
                .id("p2")
                .exam(exam)
                .user(user)
                .completed(false)
                .startedAt(LocalDateTime.now().minusMinutes(60))
                .build();
        SubmitExamRequest request = SubmitExamRequest.builder()
                .participantId("p2")
                .answers(List.of())
                .build();

        when(examParticipantRepository.findById("p2")).thenReturn(Optional.of(participant));
        when(examCacheService.getQuestions("exam-1")).thenReturn(Map.of());
        when(examCacheService.getSections("exam-1")).thenReturn(Map.of());
        when(examParticipantRepository.save(any(ExamParticipant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        SubmitExamResponse response = examSubmitService.submitExam(request);

        // Then
        assertTrue(response.getCompleted());
        assertEquals(0f, response.getTotalScore());
        assertEquals(0, response.getTotalQuestions());
    }

    @Test
    @DisplayName("TC20 - Skill analysis chart is returned after submitting an exam")
    void tc20_view_skill_analysis() {
        // Given
        User user = User.builder().id("u1").email("student@example.com").build();
        UserResponse userResponse = UserResponse.builder().id("u1").email("student@example.com").fullName("Student").build();
        UserLearningProgress progress = UserLearningProgress.builder()
                .user(user)
                .level("N3")
                .totalExamsTaken(3)
                .totalQuestionsDone(30)
                .correctQuestions(24)
                .lastExamAt(LocalDateTime.now())
                .build();

        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);
        when(userLearningProgressRepository.findByUserId("u1")).thenReturn(List.of(progress));

        // When
        AdminUserProgressResponse response = adminLearningService.getUserProgress("u1");

        // Then
        assertEquals(3, response.getTotalExamsTaken());
        assertEquals(30, response.getTotalQuestionsDone());
        assertEquals(24, response.getCorrectQuestions());
        assertEquals("N3", response.getLastLevel());
        assertEquals(1, response.getLevels().size());
        assertEquals("u1", response.getUser().getId());
    }

    @Test
    @DisplayName("TC21 - Learning history dashboard is returned in time order")
    void tc21_track_exam_history() {
        // Given
        User user = User.builder().id("u1").email("student@example.com").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("u1", null, List.of()));

        UserLearningProgress first = UserLearningProgress.builder()
                .id("p1")
                .user(user)
                .level("N4")
                .totalExamsTaken(1)
                .totalQuestionsDone(10)
                .correctQuestions(8)
                .lastExamAt(LocalDateTime.now().minusDays(2))
                .build();
        UserLearningProgress second = UserLearningProgress.builder()
                .id("p2")
                .user(user)
                .level("N3")
                .totalExamsTaken(2)
                .totalQuestionsDone(20)
                .correctQuestions(15)
                .lastExamAt(LocalDateTime.now().minusDays(1))
                .build();

        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(userLearningProgressRepository.findByUserId("u1")).thenReturn(List.of(first, second));

        // When
        UserLearningDashboardResponse response = userLearningProgressService.getDashboard();

        // Then
        assertEquals("u1", response.getUserId());
        assertEquals(3, response.getTotalExamsTaken());
        assertEquals(30, response.getTotalQuestionsDone());
        assertEquals(23, response.getCorrectQuestions());
        assertEquals("N3", response.getLastLevel());
        assertEquals(2, response.getLevels().size());
    }
}
