package com.shoppeclone.backend.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppeclone.backend.auth.dto.request.ForgotPasswordRequest;
import com.shoppeclone.backend.auth.dto.request.LoginRequest;
import com.shoppeclone.backend.auth.dto.request.RegisterRequest;
import com.shoppeclone.backend.auth.dto.request.SendOtpRequest;
import com.shoppeclone.backend.auth.dto.request.VerifyOtpRequest;
import com.shoppeclone.backend.auth.dto.response.AuthResponse;
import com.shoppeclone.backend.auth.exception.DuplicateEmailException;
import com.shoppeclone.backend.auth.exception.InvalidCredentialsException;
import com.shoppeclone.backend.auth.exception.InvalidOtpException;
import com.shoppeclone.backend.auth.exception.RateLimitExceededException;
import com.shoppeclone.backend.auth.security.JwtAuthFilter;
import com.shoppeclone.backend.auth.service.AuthRateLimitService;
import com.shoppeclone.backend.auth.service.AuthService;
import com.shoppeclone.backend.auth.service.OtpService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private AuthRateLimitService authRateLimitService;

    @MockBean
    private OtpService otpService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void register_whenRequestIsValid_returnsSuccessResponse() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setPassword("Password1");
        request.setFullName("New User");
        request.setPhone("0900000000");

        AuthResponse response = new AuthResponse();
        response.setMessage("Registration successful! OTP code has been sent to your email.");

        when(authService.register(request)).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Registration successful! OTP code has been sent to your email."));
    }

    @Test
    void register_whenEmailAlreadyExists_returnsConflictWithExplicitMessage() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("Password1");
        request.setFullName("Existing User");
        request.setPhone("0900000000");

        when(authService.register(request)).thenThrow(new DuplicateEmailException("Email already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists"))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void login_whenCredentialsInvalid_returnsUnauthorizedAndRecordsFailure() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("missing@example.com");
        request.setPassword("Password1");

        doNothing().when(authRateLimitService).checkLoginAllowed("127.0.0.1", "missing@example.com");
        when(authService.login(request)).thenThrow(new InvalidCredentialsException("Invalid email or password"));
        doNothing().when(authRateLimitService).recordLoginFailure("127.0.0.1", "missing@example.com");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"))
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        verify(authRateLimitService).checkLoginAllowed("127.0.0.1", "missing@example.com");
        verify(authRateLimitService).recordLoginFailure("127.0.0.1", "missing@example.com");
    }

    @Test
    void login_whenRateLimited_returnsTooManyRequests() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("blocked@example.com");
        request.setPassword("Password1");

        doThrow(new RateLimitExceededException("Too many failed login attempts. Try again later.", 120))
                .when(authRateLimitService).checkLoginAllowed("127.0.0.1", "blocked@example.com");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("Too many failed login attempts. Try again later."))
                .andExpect(jsonPath("$.retryAfterSeconds").value(120));

        verify(authService, never()).login(request);
    }

    @Test
    void forgotPassword_returnsGenericSuccessMessage() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("missing@example.com");

        doNothing().when(authRateLimitService).consumeOtpSendAttempt("127.0.0.1", "missing@example.com");
        doNothing().when(authService).forgotPassword("missing@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OTP code has been sent to your email"));
    }

    @Test
    void sendOtp_returnsGenericSuccessMessage() throws Exception {
        SendOtpRequest request = new SendOtpRequest();
        request.setEmail("missing@example.com");

        doNothing().when(authRateLimitService).consumeOtpSendAttempt("127.0.0.1", "missing@example.com");
        doNothing().when(otpService).sendOtpEmail("missing@example.com");

        mockMvc.perform(post("/api/auth/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OTP code has been sent to your email"));
    }

    @Test
    void verifyOtp_whenOtpInvalid_returnsBadRequestAndRecordsFailure() throws Exception {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("user@example.com");
        request.setCode("123456");

        doNothing().when(authRateLimitService).checkOtpVerificationAllowed("127.0.0.1", "user@example.com");
        doThrow(new InvalidOtpException("Invalid OTP code"))
                .when(otpService).verifyOtp("user@example.com", "123456");
        doNothing().when(authRateLimitService).recordOtpVerificationFailure("127.0.0.1", "user@example.com");

        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid OTP code"))
                .andExpect(jsonPath("$.error").value("Bad Request"));

        verify(authRateLimitService).recordOtpVerificationFailure("127.0.0.1", "user@example.com");
    }
}
