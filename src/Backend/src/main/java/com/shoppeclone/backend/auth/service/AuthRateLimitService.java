package com.shoppeclone.backend.auth.service;

public interface AuthRateLimitService {
    void checkLoginAllowed(String ipAddress, String email);

    void recordLoginFailure(String ipAddress, String email);

    void resetLoginFailures(String ipAddress, String email);

    void consumeOtpSendAttempt(String ipAddress, String email);

    void checkOtpVerificationAllowed(String ipAddress, String email);

    void recordOtpVerificationFailure(String ipAddress, String email);

    void resetOtpVerificationFailures(String ipAddress, String email);
}
