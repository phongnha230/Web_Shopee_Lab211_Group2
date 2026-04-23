package com.shoppeclone.backend.auth.service.impl;

import com.shoppeclone.backend.auth.dto.request.LoginRequest;
import com.shoppeclone.backend.auth.dto.request.RegisterRequest;
import com.shoppeclone.backend.auth.dto.response.AuthResponse;
import com.shoppeclone.backend.auth.exception.DuplicateEmailException;
import com.shoppeclone.backend.auth.exception.InvalidCredentialsException;
import com.shoppeclone.backend.auth.model.Role;
import com.shoppeclone.backend.auth.model.User;
import com.shoppeclone.backend.auth.model.UserSession;
import com.shoppeclone.backend.auth.repository.OtpCodeRepository;
import com.shoppeclone.backend.auth.repository.RoleRepository;
import com.shoppeclone.backend.auth.repository.UserRepository;
import com.shoppeclone.backend.auth.repository.UserSessionRepository;
import com.shoppeclone.backend.auth.security.JwtUtil;
import com.shoppeclone.backend.auth.service.OtpService;
import com.shoppeclone.backend.common.service.EmailService;
import com.shoppeclone.backend.user.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserSessionRepository sessionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private OtpService otpService;

    @Mock
    private OtpCodeRepository otpCodeRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_whenRequestIsValid_createsPendingUserAndSendsOtp() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setPassword("Password1");
        request.setFullName("New User");
        request.setPhone("0123456789");

        Role role = new Role();
        role.setName("ROLE_USER");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("encoded-password");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(role));
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        verify(otpService).sendOtpEmail("new@example.com");

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("new@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.isEmailVerified()).isFalse();
        assertThat(savedUser.isActive()).isTrue();
        assertThat(savedUser.getRoles()).extracting(Role::getName).containsExactly("ROLE_USER");

        assertThat(response.getAccessToken()).isNull();
        assertThat(response.getUser().getEmail()).isEqualTo("new@example.com");
        assertThat(response.getMessage()).contains("OTP code has been sent");
    }

    @Test
    void register_whenEmailAlreadyExists_keepsExplicitMessage() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("Password1");
        request.setFullName("Existing User");
        request.setPhone("0123456789");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("Email already exists");
    }

    @Test
    void login_whenEmailDoesNotExist_returnsGenericInvalidCredentialsMessage() {
        LoginRequest request = new LoginRequest();
        request.setEmail("missing@example.com");
        request.setPassword("Password1");

        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void login_whenCredentialsAreValid_returnsTokensAndSavesSession() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("Password1");

        Role role = new Role();
        role.setName("ROLE_USER");

        User user = new User();
        user.setId("user-1");
        user.setEmail("user@example.com");
        user.setPassword("encoded-password");
        user.setFullName("User One");
        user.setEmailVerified(true);
        user.setActive(true);
        user.setRoles(Set.of(role));

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password1", "encoded-password")).thenReturn(true);
        when(jwtUtil.generateAccessToken("user@example.com")).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken("user@example.com")).thenReturn("refresh-token");
        when(sessionRepository.save(org.mockito.ArgumentMatchers.any(UserSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.login(request);

        ArgumentCaptor<UserSession> sessionCaptor = ArgumentCaptor.forClass(UserSession.class);
        verify(sessionRepository).save(sessionCaptor.capture());

        UserSession savedSession = sessionCaptor.getValue();
        assertThat(savedSession.getUser()).isSameAs(user);
        assertThat(savedSession.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(savedSession.getExpiresAt()).isNotNull();

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUser().getEmail()).isEqualTo("user@example.com");
        assertThat(response.getUser().getRoles()).containsExactly("ROLE_USER");
    }

    @Test
    void forgotPassword_whenEmailDoesNotExist_returnsSilently() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        authService.forgotPassword("missing@example.com");

        verify(otpCodeRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(emailService, never()).sendPasswordResetOtp(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }
}
