package com.shoppeclone.backend.auth.controller;

import com.shoppeclone.backend.auth.dto.request.ForgotPasswordRequest;
import com.shoppeclone.backend.auth.dto.request.LoginRequest;
import com.shoppeclone.backend.auth.dto.request.RegisterRequest;
import com.shoppeclone.backend.auth.dto.request.SendOtpRequest;
import com.shoppeclone.backend.auth.dto.request.VerifyOtpRequest;
import com.shoppeclone.backend.auth.dto.request.VerifyOtpAndResetPasswordRequest;
import com.shoppeclone.backend.auth.dto.response.AuthResponse;
import com.shoppeclone.backend.auth.service.AuthService;
import com.shoppeclone.backend.auth.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
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
    public ResponseEntity<Map<String, String>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        otpService.sendOtpEmail(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "OTP code has been sent to your email"));
    }

    // ✅ MỚI: Verify OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        otpService.verifyOtp(request.getEmail(), request.getCode());
        return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
    }

    // ✅ FORGOT PASSWORD - Send OTP
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "OTP code has been sent to your email"));
    }

    // ✅ VERIFY OTP AND RESET PASSWORD
    @PostMapping("/verify-otp-reset-password")
    public ResponseEntity<Map<String, String>> verifyOtpAndResetPassword(
            @Valid @RequestBody VerifyOtpAndResetPasswordRequest request) {

        // Validate password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Password confirmation does not match");
        }

        authService.verifyOtpAndResetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }
}