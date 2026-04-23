package com.shoppeclone.backend.auth.service.impl;

import com.shoppeclone.backend.auth.exception.RateLimitExceededException;
import com.shoppeclone.backend.auth.model.RateLimitRecord;
import com.shoppeclone.backend.auth.repository.RateLimitRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthRateLimitServiceImplTest {

    @Mock
    private RateLimitRecordRepository rateLimitRecordRepository;

    @InjectMocks
    private AuthRateLimitServiceImpl authRateLimitService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authRateLimitService, "loginEmailMaxFailures", 5);
        ReflectionTestUtils.setField(authRateLimitService, "loginIpMaxFailures", 20);
        ReflectionTestUtils.setField(authRateLimitService, "loginWindowMinutes", 15L);
        ReflectionTestUtils.setField(authRateLimitService, "loginBlockMinutes", 15L);
        ReflectionTestUtils.setField(authRateLimitService, "otpSendEmailMaxAttempts", 5);
        ReflectionTestUtils.setField(authRateLimitService, "otpSendIpMaxAttempts", 20);
        ReflectionTestUtils.setField(authRateLimitService, "otpSendWindowMinutes", 60L);
        ReflectionTestUtils.setField(authRateLimitService, "otpSendBlockMinutes", 15L);
        ReflectionTestUtils.setField(authRateLimitService, "otpSendCooldownSeconds", 60L);
        ReflectionTestUtils.setField(authRateLimitService, "otpVerifyEmailMaxFailures", 5);
        ReflectionTestUtils.setField(authRateLimitService, "otpVerifyIpMaxFailures", 20);
        ReflectionTestUtils.setField(authRateLimitService, "otpVerifyWindowMinutes", 10L);
        ReflectionTestUtils.setField(authRateLimitService, "otpVerifyBlockMinutes", 15L);
    }

    @Test
    void recordLoginFailure_blocksAfterConfiguredThreshold() {
        RateLimitRecord emailRecord = new RateLimitRecord();
        emailRecord.setId("LOGIN_EMAIL:user@example.com");
        emailRecord.setAction("LOGIN_EMAIL");
        emailRecord.setSubjectKey("user@example.com");
        emailRecord.setAttempts(4);
        emailRecord.setWindowStart(LocalDateTime.now().minusMinutes(1));

        when(rateLimitRecordRepository.findById("LOGIN_EMAIL:user@example.com")).thenReturn(Optional.of(emailRecord));
        when(rateLimitRecordRepository.save(any(RateLimitRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThatThrownBy(() -> authRateLimitService.recordLoginFailure("127.0.0.1", "user@example.com"))
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessage("Too many failed login attempts. Try again later.");

        verify(rateLimitRecordRepository).save(emailRecord);
        assertThat(emailRecord.getAttempts()).isEqualTo(5);
        assertThat(emailRecord.getBlockedUntil()).isNotNull();
    }

    @Test
    void consumeOtpSendAttempt_rejectsWhenCooldownHasNotElapsed() {
        RateLimitRecord emailRecord = new RateLimitRecord();
        emailRecord.setId("OTP_SEND_EMAIL:user@example.com");
        emailRecord.setAction("OTP_SEND_EMAIL");
        emailRecord.setSubjectKey("user@example.com");
        emailRecord.setAttempts(1);
        emailRecord.setWindowStart(LocalDateTime.now().minusMinutes(5));
        emailRecord.setLastAttemptAt(LocalDateTime.now().minusSeconds(10));

        when(rateLimitRecordRepository.findById("OTP_SEND_EMAIL:user@example.com")).thenReturn(Optional.of(emailRecord));

        assertThatThrownBy(() -> authRateLimitService.consumeOtpSendAttempt("127.0.0.1", "user@example.com"))
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessage("Please wait before requesting another OTP.");
    }

    @Test
    void resetOtpVerificationFailures_deletesEmailAndIpRecords() {
        authRateLimitService.resetOtpVerificationFailures("127.0.0.1", "user@example.com");

        verify(rateLimitRecordRepository).deleteById(eq("OTP_VERIFY_EMAIL:user@example.com"));
        verify(rateLimitRecordRepository).deleteById(eq("OTP_VERIFY_IP:127.0.0.1"));
    }

    @Test
    void consumeOtpSendAttempt_recordsAttemptWhenAllowed() {
        when(rateLimitRecordRepository.findById("OTP_SEND_EMAIL:user@example.com")).thenReturn(Optional.empty());
        when(rateLimitRecordRepository.findById("OTP_SEND_IP:127.0.0.1")).thenReturn(Optional.empty());
        when(rateLimitRecordRepository.save(any(RateLimitRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authRateLimitService.consumeOtpSendAttempt("127.0.0.1", "user@example.com");

        verify(rateLimitRecordRepository, org.mockito.Mockito.times(2)).save(any(RateLimitRecord.class));
    }
}
