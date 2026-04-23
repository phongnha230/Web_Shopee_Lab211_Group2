package com.shoppeclone.backend.auth.service.impl;

import com.shoppeclone.backend.auth.exception.RateLimitExceededException;
import com.shoppeclone.backend.auth.model.RateLimitRecord;
import com.shoppeclone.backend.auth.repository.RateLimitRecordRepository;
import com.shoppeclone.backend.auth.service.AuthRateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthRateLimitServiceImpl implements AuthRateLimitService {
    @Value("${auth.rate-limit.login.email.max-failures:5}")
    private int loginEmailMaxFailures;

    @Value("${auth.rate-limit.login.ip.max-failures:20}")
    private int loginIpMaxFailures;

    @Value("${auth.rate-limit.login.window-minutes:15}")
    private long loginWindowMinutes;

    @Value("${auth.rate-limit.login.block-minutes:15}")
    private long loginBlockMinutes;

    @Value("${auth.rate-limit.otp-send.email.max-attempts:5}")
    private int otpSendEmailMaxAttempts;

    @Value("${auth.rate-limit.otp-send.ip.max-attempts:20}")
    private int otpSendIpMaxAttempts;

    @Value("${auth.rate-limit.otp-send.window-minutes:60}")
    private long otpSendWindowMinutes;

    @Value("${auth.rate-limit.otp-send.block-minutes:15}")
    private long otpSendBlockMinutes;

    @Value("${auth.rate-limit.otp-send.cooldown-seconds:60}")
    private long otpSendCooldownSeconds;

    @Value("${auth.rate-limit.otp-verify.email.max-failures:5}")
    private int otpVerifyEmailMaxFailures;

    @Value("${auth.rate-limit.otp-verify.ip.max-failures:20}")
    private int otpVerifyIpMaxFailures;

    @Value("${auth.rate-limit.otp-verify.window-minutes:10}")
    private long otpVerifyWindowMinutes;

    @Value("${auth.rate-limit.otp-verify.block-minutes:15}")
    private long otpVerifyBlockMinutes;

    private final RateLimitRecordRepository rateLimitRecordRepository;

    @Override
    public void checkLoginAllowed(String ipAddress, String email) {
        assertNotBlocked(Action.LOGIN_EMAIL, normalizeEmail(email));
        assertNotBlocked(Action.LOGIN_IP, normalizeIp(ipAddress));
    }

    @Override
    public void recordLoginFailure(String ipAddress, String email) {
        registerFailure(
                Action.LOGIN_EMAIL,
                normalizeEmail(email),
                loginEmailMaxFailures,
                Duration.ofMinutes(loginWindowMinutes),
                Duration.ofMinutes(loginBlockMinutes));
        registerFailure(
                Action.LOGIN_IP,
                normalizeIp(ipAddress),
                loginIpMaxFailures,
                Duration.ofMinutes(loginWindowMinutes),
                Duration.ofMinutes(loginBlockMinutes));
    }

    @Override
    public void resetLoginFailures(String ipAddress, String email) {
        reset(Action.LOGIN_EMAIL, normalizeEmail(email));
        reset(Action.LOGIN_IP, normalizeIp(ipAddress));
    }

    @Override
    public void consumeOtpSendAttempt(String ipAddress, String email) {
        consumeAttempt(
                Action.OTP_SEND_EMAIL,
                normalizeEmail(email),
                otpSendEmailMaxAttempts,
                Duration.ofMinutes(otpSendWindowMinutes),
                Duration.ofMinutes(otpSendBlockMinutes),
                Duration.ofSeconds(otpSendCooldownSeconds));
        consumeAttempt(
                Action.OTP_SEND_IP,
                normalizeIp(ipAddress),
                otpSendIpMaxAttempts,
                Duration.ofMinutes(otpSendWindowMinutes),
                Duration.ofMinutes(otpSendBlockMinutes),
                Duration.ZERO);
    }

    @Override
    public void checkOtpVerificationAllowed(String ipAddress, String email) {
        assertNotBlocked(Action.OTP_VERIFY_EMAIL, normalizeEmail(email));
        assertNotBlocked(Action.OTP_VERIFY_IP, normalizeIp(ipAddress));
    }

    @Override
    public void recordOtpVerificationFailure(String ipAddress, String email) {
        registerFailure(
                Action.OTP_VERIFY_EMAIL,
                normalizeEmail(email),
                otpVerifyEmailMaxFailures,
                Duration.ofMinutes(otpVerifyWindowMinutes),
                Duration.ofMinutes(otpVerifyBlockMinutes));
        registerFailure(
                Action.OTP_VERIFY_IP,
                normalizeIp(ipAddress),
                otpVerifyIpMaxFailures,
                Duration.ofMinutes(otpVerifyWindowMinutes),
                Duration.ofMinutes(otpVerifyBlockMinutes));
    }

    @Override
    public void resetOtpVerificationFailures(String ipAddress, String email) {
        reset(Action.OTP_VERIFY_EMAIL, normalizeEmail(email));
        reset(Action.OTP_VERIFY_IP, normalizeIp(ipAddress));
    }

    private void consumeAttempt(
            Action action,
            String subjectKey,
            int maxAttempts,
            Duration window,
            Duration blockDuration,
            Duration cooldown) {
        LocalDateTime now = LocalDateTime.now();
        RateLimitRecord record = getOrCreate(action, subjectKey);

        enforceBlock(record, now);
        enforceCooldown(record, cooldown, now);

        if (isWithinWindow(record, now, window) && record.getAttempts() >= maxAttempts) {
            LocalDateTime blockedUntil = now.plus(blockDuration);
            record.setBlockedUntil(blockedUntil);
            record.setExpireAt(latest(blockedUntil, now.plus(window), now.plus(cooldown)));
            rateLimitRecordRepository.save(record);
            throw tooManyRequests(action.message, blockedUntil, now);
        }

        if (isWithinWindow(record, now, window)) {
            record.setAttempts(record.getAttempts() + 1);
        } else {
            record.setAttempts(1);
            record.setWindowStart(now);
        }

        record.setLastAttemptAt(now);
        record.setBlockedUntil(null);
        record.setExpireAt(latest(record.getWindowStart().plus(window), now.plus(cooldown)));
        rateLimitRecordRepository.save(record);
    }

    private void registerFailure(
            Action action,
            String subjectKey,
            int maxAttempts,
            Duration window,
            Duration blockDuration) {
        LocalDateTime now = LocalDateTime.now();
        RateLimitRecord record = getOrCreate(action, subjectKey);

        if (isWithinWindow(record, now, window)) {
            record.setAttempts(record.getAttempts() + 1);
        } else {
            record.setAttempts(1);
            record.setWindowStart(now);
        }

        record.setLastAttemptAt(now);
        if (record.getAttempts() >= maxAttempts) {
            LocalDateTime blockedUntil = now.plus(blockDuration);
            record.setBlockedUntil(blockedUntil);
            record.setExpireAt(latest(blockedUntil, record.getWindowStart().plus(window)));
            rateLimitRecordRepository.save(record);
            throw tooManyRequests(action.message, blockedUntil, now);
        }

        record.setBlockedUntil(null);
        record.setExpireAt(record.getWindowStart().plus(window));
        rateLimitRecordRepository.save(record);
    }

    private void assertNotBlocked(Action action, String subjectKey) {
        LocalDateTime now = LocalDateTime.now();
        rateLimitRecordRepository.findById(buildId(action, subjectKey))
                .ifPresent(record -> enforceBlock(record, now));
    }

    private void reset(Action action, String subjectKey) {
        rateLimitRecordRepository.deleteById(buildId(action, subjectKey));
    }

    private RateLimitRecord getOrCreate(Action action, String subjectKey) {
        return rateLimitRecordRepository.findById(buildId(action, subjectKey))
                .orElseGet(() -> {
                    RateLimitRecord record = new RateLimitRecord();
                    record.setId(buildId(action, subjectKey));
                    record.setAction(action.name());
                    record.setSubjectKey(subjectKey);
                    record.setAttempts(0);
                    return record;
                });
    }

    private void enforceBlock(RateLimitRecord record, LocalDateTime now) {
        if (record.getBlockedUntil() != null && record.getBlockedUntil().isAfter(now)) {
            throw tooManyRequests(actionMessage(record.getAction()), record.getBlockedUntil(), now);
        }
    }

    private void enforceCooldown(RateLimitRecord record, Duration cooldown, LocalDateTime now) {
        if (cooldown.isZero() || record.getLastAttemptAt() == null) {
            return;
        }

        LocalDateTime nextAllowedAt = record.getLastAttemptAt().plus(cooldown);
        if (nextAllowedAt.isAfter(now)) {
            throw new RateLimitExceededException(
                    "Please wait before requesting another OTP.",
                    Duration.between(now, nextAllowedAt).toSeconds() + 1);
        }
    }

    private boolean isWithinWindow(RateLimitRecord record, LocalDateTime now, Duration window) {
        return record.getWindowStart() != null && !record.getWindowStart().plus(window).isBefore(now);
    }

    private RateLimitExceededException tooManyRequests(String message, LocalDateTime blockedUntil, LocalDateTime now) {
        long retryAfterSeconds = Math.max(1, Duration.between(now, blockedUntil).toSeconds());
        return new RateLimitExceededException(message, retryAfterSeconds);
    }

    private String actionMessage(String actionName) {
        return Action.valueOf(actionName).message;
    }

    private LocalDateTime latest(LocalDateTime first, LocalDateTime second) {
        return first.isAfter(second) ? first : second;
    }

    private LocalDateTime latest(LocalDateTime first, LocalDateTime second, LocalDateTime third) {
        return latest(latest(first, second), third);
    }

    private String buildId(Action action, String subjectKey) {
        return action.name() + ":" + subjectKey;
    }

    private String normalizeEmail(String email) {
        return email == null ? "unknown-email" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeIp(String ipAddress) {
        String normalized = ipAddress == null || ipAddress.isBlank() ? "unknown-ip" : ipAddress.trim();
        return normalized.toLowerCase(Locale.ROOT);
    }

    private enum Action {
        LOGIN_EMAIL("Too many failed login attempts. Try again later."),
        LOGIN_IP("Too many failed login attempts from this IP. Try again later."),
        OTP_SEND_EMAIL("Too many OTP requests. Try again later."),
        OTP_SEND_IP("Too many OTP requests from this IP. Try again later."),
        OTP_VERIFY_EMAIL("Too many invalid OTP attempts. Try again later."),
        OTP_VERIFY_IP("Too many invalid OTP attempts from this IP. Try again later.");

        private final String message;

        Action(String message) {
            this.message = message;
        }
    }
}
