package com.shoppeclone.backend.auth.controller;

import com.shoppeclone.backend.auth.dto.request.ForgotPasswordRequest;
import com.shoppeclone.backend.auth.dto.request.LoginRequest;
import com.shoppeclone.backend.auth.dto.request.RegisterRequest;
import com.shoppeclone.backend.auth.dto.request.SendOtpRequest;
import com.shoppeclone.backend.auth.dto.request.VerifyOtpRequest;
import com.shoppeclone.backend.auth.dto.request.VerifyOtpAndResetPasswordRequest;
import com.shoppeclone.backend.auth.dto.response.AuthResponse;
import com.shoppeclone.backend.auth.dto.response.UserDto;
import com.shoppeclone.backend.auth.exception.InvalidCredentialsException;
import com.shoppeclone.backend.auth.exception.InvalidOtpException;
import com.shoppeclone.backend.auth.service.AuthService;
import com.shoppeclone.backend.auth.service.AuthRateLimitService;
import com.shoppeclone.backend.auth.service.OtpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final AuthRateLimitService authRateLimitService;
    private final OtpService otpService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(authService.getCurrentUser(userDetails.getUsername()));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = extractClientIp(httpRequest);
        authRateLimitService.checkLoginAllowed(clientIp, request.getEmail());

        try {
            AuthResponse response = authService.login(request);
            authRateLimitService.resetLoginFailures(clientIp, request.getEmail());
            return ResponseEntity.ok(response);
        } catch (InvalidCredentialsException ex) {
            authRateLimitService.recordLoginFailure(clientIp, request.getEmail());
            throw ex;
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Refresh-Token") String refreshToken) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Refresh-Token") String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.ok().build();
    }

    // ✅ MỚI: Gửi OTP
    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, String>> sendOtp(
            @Valid @RequestBody SendOtpRequest request,
            HttpServletRequest httpRequest) {
        authRateLimitService.consumeOtpSendAttempt(extractClientIp(httpRequest), request.getEmail());
        otpService.sendOtpEmail(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "OTP code has been sent to your email"));
    }

    // ✅ MỚI: Verify OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = extractClientIp(httpRequest);
        authRateLimitService.checkOtpVerificationAllowed(clientIp, request.getEmail());

        try {
            otpService.verifyOtp(request.getEmail(), request.getCode());
            authRateLimitService.resetOtpVerificationFailures(clientIp, request.getEmail());
            return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
        } catch (InvalidOtpException ex) {
            authRateLimitService.recordOtpVerificationFailure(clientIp, request.getEmail());
            throw ex;
        }
    }

    // ✅ MỚI: Check OTP Reset Password (Không mark used)
    @PostMapping("/check-reset-otp")
    public ResponseEntity<Map<String, String>> checkResetOtp(
            @Valid @RequestBody VerifyOtpRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = extractClientIp(httpRequest);
        authRateLimitService.checkOtpVerificationAllowed(clientIp, request.getEmail());

        try {
            otpService.checkResetOtp(request.getEmail(), request.getCode());
            authRateLimitService.resetOtpVerificationFailures(clientIp, request.getEmail());
            return ResponseEntity.ok(Map.of("message", "OTP is valid"));
        } catch (InvalidOtpException ex) {
            authRateLimitService.recordOtpVerificationFailure(clientIp, request.getEmail());
            throw ex;
        }
    }

    // ✅ FORGOT PASSWORD - Send OTP
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest) {
        authRateLimitService.consumeOtpSendAttempt(extractClientIp(httpRequest), request.getEmail());
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "OTP code has been sent to your email"));
    }

    // ✅ VERIFY OTP AND RESET PASSWORD
    @PostMapping("/verify-otp-reset-password")
    public ResponseEntity<Map<String, String>> verifyOtpAndResetPassword(
            @Valid @RequestBody VerifyOtpAndResetPasswordRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = extractClientIp(httpRequest);
        authRateLimitService.checkOtpVerificationAllowed(clientIp, request.getEmail());

        // Validate password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Password confirmation does not match");
        }

        try {
            authService.verifyOtpAndResetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
            authRateLimitService.resetOtpVerificationFailures(clientIp, request.getEmail());
            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
        } catch (InvalidOtpException ex) {
            authRateLimitService.recordOtpVerificationFailure(clientIp, request.getEmail());
            throw ex;
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }
}
