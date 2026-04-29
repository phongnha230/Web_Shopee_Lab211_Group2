package com.shoppeclone.backend.auth.service.impl;

import com.shoppeclone.backend.auth.exception.RateLimitExceededException;
import com.shoppeclone.backend.auth.service.AuthRateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.auth.rate-limit.store", havingValue = "redis")
public class RedisAuthRateLimitServiceImpl implements AuthRateLimitService {

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

    private final StringRedisTemplate redisTemplate;

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
        long now = nowEpochMillis();
        RedisRateLimitRecord record = getOrCreate(action, subjectKey);

        enforceBlock(action, record, now);
        enforceCooldown(record, cooldown, now);

        if (isWithinWindow(record, now, window) && record.attempts >= maxAttempts) {
            long blockedUntil = now + blockDuration.toMillis();
            record.blockedUntil = blockedUntil;
            record.expireAt = latest(blockedUntil, now + window.toMillis(), now + cooldown.toMillis());
            save(action, subjectKey, record);
            throw tooManyRequests(action.message, blockedUntil, now);
        }

        if (isWithinWindow(record, now, window)) {
            record.attempts++;
        } else {
            record.attempts = 1;
            record.windowStart = now;
        }

        record.lastAttemptAt = now;
        record.blockedUntil = null;
        record.expireAt = latest(record.windowStart + window.toMillis(), now + cooldown.toMillis());
        save(action, subjectKey, record);
    }

    private void registerFailure(
            Action action,
            String subjectKey,
            int maxAttempts,
            Duration window,
            Duration blockDuration) {
        long now = nowEpochMillis();
        RedisRateLimitRecord record = getOrCreate(action, subjectKey);

        if (isWithinWindow(record, now, window)) {
            record.attempts++;
        } else {
            record.attempts = 1;
            record.windowStart = now;
        }

        record.lastAttemptAt = now;
        if (record.attempts >= maxAttempts) {
            long blockedUntil = now + blockDuration.toMillis();
            record.blockedUntil = blockedUntil;
            record.expireAt = latest(blockedUntil, record.windowStart + window.toMillis());
            save(action, subjectKey, record);
            throw tooManyRequests(action.message, blockedUntil, now);
        }

        record.blockedUntil = null;
        record.expireAt = record.windowStart + window.toMillis();
        save(action, subjectKey, record);
    }

    private void assertNotBlocked(Action action, String subjectKey) {
        RedisRateLimitRecord record = find(action, subjectKey);
        if (record != null) {
            enforceBlock(action, record, nowEpochMillis());
        }
    }

    private void reset(Action action, String subjectKey) {
        redisTemplate.delete(buildKey(action, subjectKey));
    }

    private RedisRateLimitRecord getOrCreate(Action action, String subjectKey) {
        RedisRateLimitRecord record = find(action, subjectKey);
        return record != null ? record : new RedisRateLimitRecord();
    }

    private RedisRateLimitRecord find(Action action, String subjectKey) {
        String key = buildKey(action, subjectKey);
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        Map<Object, Object> values = hashOperations.entries(key);
        if (values == null || values.isEmpty()) {
            return null;
        }

        RedisRateLimitRecord record = new RedisRateLimitRecord();
        record.attempts = parseLong(values.get("attempts"), 0L);
        record.windowStart = parseNullableLong(values.get("windowStart"));
        record.blockedUntil = parseNullableLong(values.get("blockedUntil"));
        record.lastAttemptAt = parseNullableLong(values.get("lastAttemptAt"));
        record.expireAt = parseNullableLong(values.get("expireAt"));
        return record;
    }

    private void save(Action action, String subjectKey, RedisRateLimitRecord record) {
        String key = buildKey(action, subjectKey);
        Map<String, String> values = new LinkedHashMap<>();
        values.put("attempts", Long.toString(record.attempts));
        putNullable(values, "windowStart", record.windowStart);
        putNullable(values, "blockedUntil", record.blockedUntil);
        putNullable(values, "lastAttemptAt", record.lastAttemptAt);
        putNullable(values, "expireAt", record.expireAt);

        redisTemplate.opsForHash().putAll(key, values);

        if (record.expireAt != null) {
            long ttlMillis = Math.max(1000L, record.expireAt - nowEpochMillis());
            redisTemplate.expire(key, Duration.ofMillis(ttlMillis));
        }
    }

    private void enforceBlock(Action action, RedisRateLimitRecord record, long now) {
        if (record.blockedUntil != null && record.blockedUntil > now) {
            throw tooManyRequests(action.message, record.blockedUntil, now);
        }
    }

    private void enforceCooldown(RedisRateLimitRecord record, Duration cooldown, long now) {
        if (cooldown.isZero() || record.lastAttemptAt == null) {
            return;
        }

        long nextAllowedAt = record.lastAttemptAt + cooldown.toMillis();
        if (nextAllowedAt > now) {
            throw new RateLimitExceededException(
                    "Please wait before requesting another OTP.",
                    Math.max(1, Duration.ofMillis(nextAllowedAt - now).toSeconds() + 1));
        }
    }

    private boolean isWithinWindow(RedisRateLimitRecord record, long now, Duration window) {
        return record.windowStart != null && (record.windowStart + window.toMillis()) >= now;
    }

    private RateLimitExceededException tooManyRequests(String message, long blockedUntil, long now) {
        long retryAfterSeconds = Math.max(1, Duration.ofMillis(blockedUntil - now).toSeconds());
        return new RateLimitExceededException(message, retryAfterSeconds);
    }

    private long latest(long first, long second) {
        return Math.max(first, second);
    }

    private long latest(long first, long second, long third) {
        return Math.max(first, Math.max(second, third));
    }

    private long nowEpochMillis() {
        return Instant.now().toEpochMilli();
    }

    private String buildKey(Action action, String subjectKey) {
        return "rate-limit:" + action.name() + ":" + subjectKey;
    }

    private long parseLong(Object value, long fallback) {
        if (value == null) {
            return fallback;
        }
        return Long.parseLong(value.toString());
    }

    private Long parseNullableLong(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString();
        return text.isBlank() ? null : Long.parseLong(text);
    }

    private void putNullable(Map<String, String> values, String key, Long value) {
        if (value == null) {
            values.put(key, "");
        } else {
            values.put(key, Long.toString(value));
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? "unknown-email" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeIp(String ipAddress) {
        return ipAddress == null || ipAddress.isBlank() ? "unknown-ip" : ipAddress.trim();
    }

    private enum Action {
        LOGIN_EMAIL("Too many failed login attempts. Try again later."),
        LOGIN_IP("Too many failed login attempts. Try again later."),
        OTP_SEND_EMAIL("Too many OTP requests. Try again later."),
        OTP_SEND_IP("Too many OTP requests. Try again later."),
        OTP_VERIFY_EMAIL("Too many invalid OTP attempts. Try again later."),
        OTP_VERIFY_IP("Too many invalid OTP attempts. Try again later.");

        private final String message;

        Action(String message) {
            this.message = message;
        }
    }

    private static final class RedisRateLimitRecord {
        private long attempts;
        private Long windowStart;
        private Long blockedUntil;
        private Long lastAttemptAt;
        private Long expireAt;
    }
}
