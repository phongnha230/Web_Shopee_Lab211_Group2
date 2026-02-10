package com.shoppeclone.backend.auth.service;

import com.shoppeclone.backend.auth.dto.request.LoginRequest;
import com.shoppeclone.backend.auth.dto.request.RegisterRequest;
import com.shoppeclone.backend.auth.dto.response.AuthResponse;
import com.shoppeclone.backend.auth.dto.response.UserDto;

public interface AuthService {
    UserDto getCurrentUser(String email);
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(String refreshToken);

    void logout(String refreshToken);

    void forgotPassword(String email);

    void verifyOtpAndResetPassword(String email, String otp, String newPassword);
}