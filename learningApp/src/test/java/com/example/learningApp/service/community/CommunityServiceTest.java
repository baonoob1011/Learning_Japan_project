// CommunityServiceTest.java
package com.example.learningApp.service.community;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.request.chat.CreateChatMessageRequest;
import com.example.learningApp.dto.request.chat.CreateGroupRoomRequest;
import com.example.learningApp.dto.request.friend.SendFriendRequest;
import com.example.learningApp.dto.response.chat.ChatMessageResponse;
import com.example.learningApp.dto.response.chat.ChatRoomResponse;
import com.example.learningApp.dto.response.friend.FriendRequestResponse;
import com.example.learningApp.entity.CallHistory;
import com.example.learningApp.entity.ChatMessage;
import com.example.learningApp.entity.ChatRoom;
import com.example.learningApp.entity.ChatRoomMember;
import com.example.learningApp.entity.Friendship;
import com.example.learningApp.entity.User;
import com.example.learningApp.entity.UserVocabProgress;
import com.example.learningApp.enums.FriendRequestStatus;
import com.example.learningApp.enums.NotificationType;
import com.example.learningApp.enums.RoomType;
import com.example.learningApp.mapper.ChatMessageMapper;
import com.example.learningApp.repository.CallHistoryRepository;
import com.example.learningApp.repository.ChatMessageRepository;
import com.example.learningApp.repository.ChatRoomMemberRepository;
import com.example.learningApp.repository.ChatRoomRepository;
import com.example.learningApp.repository.FriendshipRepository;
import com.example.learningApp.repository.UserRepository;
import com.example.learningApp.repository.UserVocabProgressRepository;
import com.example.learningApp.service.call.CallHistoryService;
import com.example.learningApp.service.chat.ChatMessageService;
import com.example.learningApp.service.chat.ChatRoomCommandService;
import com.example.learningApp.service.chat.ChatRoomResponseBuilder;
import com.example.learningApp.service.chat.friend.FriendService;
import com.example.learningApp.service.notification.NotificationService;
import com.example.learningApp.service.vocab.IntradayVocabReminderJob;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.isNull;

@ExtendWith(MockitoExtension.class)
class CommunityServiceTest {

    @Mock private ChatRoomRepository chatRoomRepository;
    @Mock private ChatRoomMemberRepository memberRepository;
    @Mock private EntityFinder finder;
    @Mock private com.example.learningApp.service.cloud.S3Service s3Service;
    @Mock private ChatRoomResponseBuilder responseBuilder;
    @Mock private UserRepository userRepository;
    @Mock private ChatMessageRepository chatMessageRepository;
    @Mock private ChatMessageMapper chatMessageMapper;
    @Mock private FriendshipRepository friendshipRepository;
    @Mock private CallHistoryRepository callHistoryRepository;
    @Mock private NotificationService notificationService;
    @Mock private UserVocabProgressRepository progressRepo;
    @Mock private SimpMessagingTemplate messagingTemplate;

    @Test
    @DisplayName("TC26 - Creating a group room adds the creator and members")
    void tc26_create_group_room() throws Exception {
        // Given
        User currentUser = User.builder().id("u1").fullName("Owner").build();
        User otherUser = User.builder().id("u2").fullName("Member").build();
        ChatRoom room = ChatRoom.builder().id("room-1").name("Study Group").roomType(RoomType.GROUP).build();
        ChatRoomResponse expected = ChatRoomResponse.builder().id("room-1").name("Study Group").roomType("GROUP").build();

        CreateGroupRoomRequest request = new CreateGroupRoomRequest();
        request.setName("Study Group");
        request.setMemberIds(List.of("u2"));
        request.setAvatar(null);

        ChatRoomCommandService service = new ChatRoomCommandService(chatRoomRepository, memberRepository, finder, s3Service, responseBuilder, userRepository);
        when(finder.userById()).thenReturn(currentUser);
        when(finder.userId("u1")).thenReturn(currentUser);
        when(finder.userId("u2")).thenReturn(otherUser);
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(room);
        when(responseBuilder.build(any(ChatRoom.class), eq(currentUser))).thenReturn(expected);

        // When
        ChatRoomResponse response = service.createGroupRoom(request);

        // Then
        assertEquals("room-1", response.getId());
        assertEquals("Study Group", response.getName());
        verify(chatRoomRepository).save(any(ChatRoom.class));
        verify(memberRepository, times(2)).save(any(ChatRoomMember.class));
    }

    @Test
    @DisplayName("TC27 - Realtime chat message is broadcast over WebSocket")
    void tc27_send_and_receive_realtime_message() {
        // Given
        User sender = User.builder().id("u1").fullName("Sender").build();
        ChatRoom room = ChatRoom.builder().id("room-1").roomType(RoomType.PRIVATE).build();
        CreateChatMessageRequest request = new CreateChatMessageRequest();
        request.setRoomId("room-1");
        request.setContent("Hello");
        ChatMessage message = ChatMessage.builder().room(room).sender(sender).content("Hello").build();
        ChatMessageResponse response = new ChatMessageResponse();
        response.setRoomId("room-1");
        response.setSenderId("u1");
        response.setContent("Hello");

        ChatMessageService service = new ChatMessageService(messagingTemplate, chatMessageMapper, chatMessageRepository, memberRepository, finder);
        when(finder.chatRoomById("room-1")).thenReturn(room);
        when(finder.userId("u1")).thenReturn(sender);
        when(memberRepository.existsByRoomIdAndUserId("room-1", "u1")).thenReturn(true);
        when(chatMessageMapper.toChatMessage(request)).thenReturn(message);
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(chatMessageMapper.toChatMessageResponse(any(ChatMessage.class))).thenReturn(response);

        // When
        ChatMessageResponse actual = service.saveAndSend(request, () -> "u1");

        // Then
        assertEquals("Hello", actual.getContent());
        verify(messagingTemplate).convertAndSend("/topic/room/room-1", response);
    }

    @Test
    @DisplayName("TC28 - Sending a friend request creates notification immediately")
    void tc28_friend_request_notification() {
        // Given
        User sender = User.builder().id("u1").fullName("Sender").avatarUrl("s.png").build();
        User receiver = User.builder().id("u2").fullName("Receiver").avatarUrl("r.png").build();
        SendFriendRequest request = new SendFriendRequest();
        request.setReceiverId("u2");

        Friendship friendship = Friendship.builder()
                .id("fr-1")
                .user1(sender)
                .user2(receiver)
                .status(FriendRequestStatus.PENDING)
                .build();

        FriendService service = new FriendService(friendshipRepository, chatRoomRepository, memberRepository, chatMessageRepository, finder, messagingTemplate);
        when(finder.userById()).thenReturn(sender);
        when(finder.userId("u2")).thenReturn(receiver);
        when(friendshipRepository.findByUser1AndUser2(any(User.class), any(User.class))).thenReturn(Optional.empty());
        when(friendshipRepository.save(any(Friendship.class))).thenReturn(friendship);

        // When
        FriendRequestResponse response = service.sendRequest(request);

        // Then
        assertEquals("fr-1", response.getRequestId());
        assertEquals("u1", response.getSenderId());
        assertEquals("u2", response.getReceiverId());
        assertEquals("PENDING", response.getStatus());
        verify(messagingTemplate).convertAndSend(eq("/topic/friend-request/u2"), any(FriendRequestResponse.class));
    }

    @Test
    @DisplayName("TC29 - Video call history is saved and delivered through chat log")
    void tc29_video_call_between_two_users() {
        // Given
        User caller = User.builder().id("u1").fullName("Caller").build();
        User receiver = User.builder().id("u2").fullName("Receiver").build();
        ChatRoom room = ChatRoom.builder().id("room-1").privateKey("u1_u2").roomType(RoomType.PRIVATE).build();
        CallHistory saved = CallHistory.builder().id("call-1").caller(caller).receiver(receiver).status("COMPLETED").type("VIDEO").duration(60).roomId("room-1").build();
        ChatMessage message = ChatMessage.builder().room(room).sender(caller).content("call").build();
        ChatMessageResponse response = new ChatMessageResponse();
        response.setRoomId("room-1");
        response.setSenderId("u1");
        response.setReceiverId("u2");
        response.setContent("call");

        CallHistoryService service = new CallHistoryService(callHistoryRepository, userRepository, notificationService, chatRoomRepository, chatMessageRepository, messagingTemplate, chatMessageMapper);
        when(userRepository.findById("u1")).thenReturn(Optional.of(caller));
        when(userRepository.findById("u2")).thenReturn(Optional.of(receiver));
        when(callHistoryRepository.save(any(CallHistory.class))).thenReturn(saved);
        when(chatRoomRepository.findByPrivateKey("u1_u2")).thenReturn(Optional.of(room));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(message);
        when(chatMessageMapper.toChatMessageResponse(any(ChatMessage.class))).thenReturn(response);

        // When
        CallHistory history = service.saveCallRecord("u1", "u2", "VIDEO", "COMPLETED", 60, "room-1");

        // Then
        assertEquals("call-1", history.getId());
        verify(messagingTemplate).convertAndSend("/topic/room/room-1", response);
        verify(notificationService, never()).create(any(User.class), anyString(), anyString());
    }

    @Test
    @DisplayName("TC30 - Vocabulary reminder notifications are pushed for due words")
    void tc30_vocab_reminder_notification() {
        // Given
        User user = User.builder().id("u1").email("student@example.com").lastReminderSentAt(null).build();
        UserVocabProgress progress = UserVocabProgress.builder()
                .id("p1")
                .user(user)
                .nextReviewAt(LocalDateTime.now().minusHours(1))
                .build();

        IntradayVocabReminderJob job = new IntradayVocabReminderJob(progressRepo, userRepository, notificationService);
        when(progressRepo.findAllByNextReviewAtBefore(any(LocalDateTime.class))).thenReturn(List.of(progress));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        job.sendIntradayReminders();

        // Then
        verify(notificationService).create(eq(user), anyString(), anyString(), eq(NotificationType.REVIEW_REMINDER), isNull());
        verify(userRepository).save(user);
        assertNotNull(user.getLastReminderSentAt());
    }
}
