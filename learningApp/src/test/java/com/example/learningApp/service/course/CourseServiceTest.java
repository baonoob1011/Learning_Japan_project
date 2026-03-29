// CourseServiceTest.java
package com.example.learningApp.service.course;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.request.course.CreateCourseRequest;
import com.example.learningApp.dto.request.vocab.MarkVocabRequest;
import com.example.learningApp.dto.response.course.CourseResponse;
import com.example.learningApp.entity.Course;
import com.example.learningApp.entity.Order;
import com.example.learningApp.entity.OrderItem;
import com.example.learningApp.entity.User;
import com.example.learningApp.entity.UserCourseProgress;
import com.example.learningApp.entity.Vocab;
import com.example.learningApp.enums.JLPTLevel;
import com.example.learningApp.enums.LessonProcess;
import com.example.learningApp.enums.ProductType;
import com.example.learningApp.enums.StudyMode;
import com.example.learningApp.mapper.CourseMapper;
import com.example.learningApp.mapper.OrderMapper;
import com.example.learningApp.repository.CourseRepository;
import com.example.learningApp.repository.EnrollmentRepository;
import com.example.learningApp.repository.OrderRepository;
import com.example.learningApp.repository.UserCourseProgressRepository;
import com.example.learningApp.repository.UserRepository;
import com.example.learningApp.repository.UserSectionProgressRepository;
import com.example.learningApp.repository.UserVocabProgressRepository;
import com.example.learningApp.repository.VocabRepository;
import com.example.learningApp.service.order.OrderService;
import com.example.learningApp.service.review.SrsGradingService;
import com.example.learningApp.service.review.ReviewSessionService;
import com.example.learningApp.service.vocab.TrackingVocabLearningService;
import com.example.learningApp.service.vipPackage.VipPurchaseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock private CourseRepository courseRepository;
    @Mock private CourseMapper courseMapper;
    @Mock private EntityFinder entityFinder;
    @Mock private com.example.learningApp.service.cloud.S3Service s3Service;

    @Mock private OrderRepository orderRepository;
    @Mock private VipPurchaseService vipPurchaseService;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private com.example.learningApp.repository.VipSubscriptionRepository vipSubscriptionRepository;
    @Mock private OrderMapper orderMapper;

    @Mock private UserSectionProgressRepository userSectionProgressRepository;
    @Mock private UserCourseProgressRepository userCourseProgressRepository;
    @Mock private UserVocabProgressRepository progressRepo;
    @Mock private UserRepository userRepository;
    @Mock private VocabRepository vocabRepository;
    @Mock private ReviewSessionService reviewSessionService;

    private final SrsGradingService srsGradingService = new SrsGradingService();

    private CourseService courseService;
    private OrderService orderService;
    private UserCourseProgressService userCourseProgressService;
    private TrackingVocabLearningService trackingVocabLearningService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        courseService = new CourseService(courseRepository, courseMapper, entityFinder, s3Service);
        orderService = new OrderService(orderRepository, vipPurchaseService, enrollmentRepository, vipSubscriptionRepository, orderMapper, entityFinder);
        userCourseProgressService = new UserCourseProgressService(userSectionProgressRepository, userCourseProgressRepository, entityFinder, courseMapper);
        trackingVocabLearningService = new TrackingVocabLearningService(
                progressRepo,
                entityFinder,
                srsGradingService,
                userRepository,
                reviewSessionService
        );
    }

    @Test
    @DisplayName("TC13 - Free course registration creates enrollment data immediately")
    void tc13_register_free_course() {
        // Given
        User creator = User.builder().id("u1").email("creator@example.com").build();
        Course course = Course.builder()
                .title("N5 Starter")
                .description("Free course")
                .level(JLPTLevel.N5)
                .lessonProcess(LessonProcess.JUNBI)
                .build();
        CreateCourseRequest request = new CreateCourseRequest();
        request.setTitle("N5 Starter");
        request.setDescription("Free course");
        request.setIsPaid(false);
        request.setPrice(0L);
        request.setLevel(JLPTLevel.N5);
        request.setLessonProcess(LessonProcess.JUNBI);

        when(entityFinder.userById()).thenReturn(creator);
        when(courseMapper.toCourse(request)).thenReturn(course);
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        String result = courseService.createCourse(request);

        // Then
        assertEquals("Create course successfully", result);
        ArgumentCaptor<Course> captor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).save(captor.capture());
        assertEquals(Boolean.FALSE, captor.getValue().getIsPaid());
        assertEquals(0L, captor.getValue().getPrice());
        assertEquals(creator, captor.getValue().getCreatedBy());
        assertTrue(Boolean.TRUE.equals(captor.getValue().getIsActive()));
    }

    @Test
    @DisplayName("TC14 - Successful course payment creates enrollment")
    void tc14_pay_course_vnpay_success() {
        // Given
        User user = User.builder().id("u1").email("student@example.com").build();
        Course course = Course.builder().id("c1").title("Paid course").isPaid(true).price(200000L).build();
        Order order = Order.builder()
                .id("o1")
                .user(user)
                .orderCode("ORD-1")
                .amount(200000L)
                .orderItems(new java.util.ArrayList<>())
                .build();
        OrderItem item = OrderItem.builder()
                .order(order)
                .productType(ProductType.COURSE)
                .course(course)
                .price(200000L)
                .build();
        order.getOrderItems().add(item);

        when(orderRepository.findByOrderCode("ORD-1")).thenReturn(Optional.of(order));
        when(enrollmentRepository.existsByUserIdAndCourseId("u1", "c1")).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toOrderSuccessResponse(any(Order.class))).thenReturn(
                com.example.learningApp.dto.response.order.OrderSuccessResponse.builder()
                        .orderCode("ORD-1")
                        .build());

        // When
        var response = orderService.markOrderSuccess("ORD-1", "TXN-1");

        // Then
        assertEquals("ORD-1", response.getOrderCode());
        verify(enrollmentRepository).save(any(com.example.learningApp.entity.Enrollment.class));
        assertEquals(com.example.learningApp.enums.PaymentStatus.SUCCESS, order.getStatus());
    }

    @Test
    @DisplayName("TC15 - Course progress percentage is updated after each lesson")
    void tc15_track_course_progress() {
        // Given
        User user = User.builder().id("u1").build();
        Course course = Course.builder().id("c1").title("Course").build();

        when(userSectionProgressRepository.calculateCoursePercent("u1", "c1")).thenReturn(87.345);
        when(userCourseProgressRepository.findByUserIdAndCourseId("u1", "c1")).thenReturn(Optional.empty());
        when(userCourseProgressRepository.save(any(UserCourseProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        userCourseProgressService.updateCourseProgress(user, course);

        // Then
        ArgumentCaptor<UserCourseProgress> captor = ArgumentCaptor.forClass(UserCourseProgress.class);
        verify(userCourseProgressRepository).save(captor.capture());
        assertEquals(87.35, captor.getValue().getProgressPercent());
        assertFalse(Boolean.TRUE.equals(captor.getValue().getCompleted()));
    }

    @Test
    @DisplayName("TC16 - Flashcard review updates SRS and session state")
    void tc16_flashcard_srs() {
        // Given
        User user = User.builder().id("u1").email("student@example.com").savedVocabs(new HashSet<>()).build();
        Vocab vocab = Vocab.builder().id("v1").surface("食べる").build();
        user.getSavedVocabs().add(vocab);

        when(entityFinder.userById()).thenReturn(user);
        when(entityFinder.vocabId("v1")).thenReturn(vocab);
        when(progressRepo.findByUserAndVocab_Id(user, "v1")).thenReturn(Optional.empty());
        when(progressRepo.save(any(com.example.learningApp.entity.UserVocabProgress.class))).thenAnswer(invocation -> {
            com.example.learningApp.entity.UserVocabProgress saved = invocation.getArgument(0);
            saved.setId("progress-1");
            return saved;
        });

        // When
        trackingVocabLearningService.markVocab(MarkVocabRequest.builder().vocabId("v1").studyMode(StudyMode.FLASHCARD).remembered(true).build());

        // Then
        ArgumentCaptor<com.example.learningApp.entity.UserVocabProgress> captor =
                ArgumentCaptor.forClass(com.example.learningApp.entity.UserVocabProgress.class);
        verify(progressRepo).save(captor.capture());
        com.example.learningApp.entity.UserVocabProgress progress = captor.getValue();
        assertEquals(10, progress.getReadingScore());
        assertEquals(3, progress.getMasteryLevel());
        assertTrue(progress.getNextReviewAt().isAfter(LocalDateTime.now().minusMinutes(1)));
        verify(reviewSessionService, atLeastOnce()).markItemCompletedIfInTodaySession(eq(user), anyString());
    }
}
