package com.shoppeclone.backend.auth.service;

public interface OtpService {
    void sendOtpEmail(String email);

    void verifyOtp(String email, String code);

    void checkResetOtp(String email, String code);
}