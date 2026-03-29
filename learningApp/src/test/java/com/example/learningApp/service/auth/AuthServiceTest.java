// AuthServiceTest.java
package com.example.learningApp.service.auth;

import com.example.learningApp.configuration.security.SingleSessionFilter;
import com.example.learningApp.dto.request.auth.UserLoginRequest;
import com.example.learningApp.dto.request.user.CreateUserRequest;
import com.example.learningApp.dto.response.user.UserLoginResponse;
import com.example.learningApp.dto.response.user.UserResponse;
import com.example.learningApp.entity.User;
import com.example.learningApp.mapper.UserMapper;
import com.example.learningApp.repository.UserRepository;
import com.example.learningApp.service.chat.ChatRoomCommandService;
import com.example.learningApp.service.role.RoleService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminSetUserPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CognitoIdentityProviderClient cognitoClient;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleService roleService;

    @Mock
    private ChatRoomCommandService chatRoomCommandService;

    @Mock
    private SessionService sessionService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "clientId", "client-id");
        ReflectionTestUtils.setField(authService, "clientSecret", "client-secret");
        ReflectionTestUtils.setField(authService, "userPoolId", "user-pool-id");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("TC01 - Register new account successfully")
    void tc01_register_success() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .email("student@example.com")
                .fullName("Student One")
                .password("Password@123")
                .build();

        User savedUser = User.builder()
                .id("sub-123")
                .email(request.getEmail())
                .fullName(request.getFullName())
                .enabled(true)
                .build();

        UserResponse expected = UserResponse.builder()
                .id("sub-123")
                .email(request.getEmail())
                .fullName(request.getFullName())
                .enabled(true)
                .roles(List.of("USER"))
                .build();

        AdminCreateUserResponse createUserResponse = AdminCreateUserResponse.builder()
                .user(UserType.builder()
                        .attributes(AttributeType.builder().name("sub").value("sub-123").build())
                        .build())
                .build();

        when(cognitoClient.adminCreateUser(any(AdminCreateUserRequest.class))).thenReturn(createUserResponse);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toUserResponse(savedUser)).thenReturn(expected);

        // When
        UserResponse response = authService.registerUser(request, false);

        // Then
        assertEquals("sub-123", response.getId());
        assertEquals("student@example.com", response.getEmail());
        assertEquals("Student One", response.getFullName());
        assertTrue(Boolean.TRUE.equals(response.getEnabled()));
        assertEquals(List.of("USER"), response.getRoles());

        ArgumentCaptor<AdminSetUserPasswordRequest> passwordCaptor = ArgumentCaptor.forClass(AdminSetUserPasswordRequest.class);
        verify(cognitoClient).adminSetUserPassword(passwordCaptor.capture());
        assertEquals("student@example.com", passwordCaptor.getValue().username());
        assertEquals("Password@123", passwordCaptor.getValue().password());
        verify(roleService).assignRoleToUser(any());
        verify(chatRoomCommandService).addUserToCommunity(savedUser);
    }

    @Test
    @DisplayName("TC02 - Valid login returns JWT tokens and session id")
    void tc02_login_success() {
        // Given
        UserLoginRequest request = UserLoginRequest.builder()
                .email("student@example.com")
                .password("Password@123")
                .build();

        User user = User.builder()
                .id("user-1")
                .email(request.getEmail())
                .fullName("Student One")
                .enabled(true)
                .build();

        AdminInitiateAuthResponse authResponse = AdminInitiateAuthResponse.builder()
                .authenticationResult(AuthenticationResultType.builder()
                        .accessToken("access-token")
                        .refreshToken("refresh-token")
                        .build())
                .build();

        when(cognitoClient.adminInitiateAuth(any(AdminInitiateAuthRequest.class))).thenReturn(authResponse);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(java.util.Optional.of(user));
        when(sessionService.initNewSession("user-1", "Desktop", "127.0.0.1")).thenReturn("session-123");

        // When
        UserLoginResponse response = authService.login(request, "Desktop", "127.0.0.1");

        // Then
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("session-123", response.getSessionId());
        verify(messagingTemplate).convertAndSend("/topic/user/user-1/kick-out", "session-123");
    }

    @Test
    @DisplayName("TC03 - Wrong password throws authentication error")
    void tc03_login_wrong_password() {
        // Given
        UserLoginRequest request = UserLoginRequest.builder()
                .email("student@example.com")
                .password("wrong-password")
                .build();

        when(cognitoClient.adminInitiateAuth(any(AdminInitiateAuthRequest.class)))
                .thenThrow(NotAuthorizedException.builder().message("Incorrect password").build());

        // When
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(request, "Desktop", "127.0.0.1"));

        // Then
        assertEquals("Invalid email or password", ex.getMessage());
        verifyNoInteractions(userRepository, sessionService, messagingTemplate);
    }

    @Test
    @DisplayName("TC04 - Refresh token is renewed before expiration")
    void tc04_refresh_token_success() {
        // Given
        AdminInitiateAuthResponse authResponse = AdminInitiateAuthResponse.builder()
                .authenticationResult(AuthenticationResultType.builder()
                        .accessToken("new-access-token")
                        .build())
                .build();
        when(cognitoClient.adminInitiateAuth(any(AdminInitiateAuthRequest.class))).thenReturn(authResponse);

        // When
        UserLoginResponse response = authService.refreshToken("student@example.com", "refresh-token");

        // Then
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
    }

    @Test
    @DisplayName("TC05 - Protected route is rejected when session is invalid")
    void tc05_protected_route_invalid_session_is_blocked() throws Exception {
        // Given
        SingleSessionFilter filter = new SingleSessionFilter(sessionService);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user-1", null, List.of()));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/myCourses");
        request.addHeader("X-Session-ID", "wrong-sid");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();

        when(sessionService.hasActiveSession("user-1")).thenReturn(true);
        when(sessionService.isSessionValid("user-1", "wrong-sid")).thenReturn(false);

        // When
        filter.doFilter(request, response, chain);

        // Then
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Session invalidated"));
        verify(sessionService).hasActiveSession("user-1");
        verify(sessionService).isSessionValid("user-1", "wrong-sid");
    }
}
