package com.shoppeclone.backend.auth.service.impl;

import com.shoppeclone.backend.auth.model.OtpCode;
import com.shoppeclone.backend.auth.repository.OtpCodeRepository;
import com.shoppeclone.backend.auth.repository.UserRepository;
import com.shoppeclone.backend.common.service.EmailService;
import com.shoppeclone.backend.user.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtpServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpCodeRepository otpCodeRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OtpServiceImpl otpService;

    @Test
    void sendOtpEmail_whenEmailDoesNotExist_returnsSilently() {
        ReflectionTestUtils.setField(otpService, "otpExpiration", 300000L);
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        otpService.sendOtpEmail("missing@example.com");

        verify(otpCodeRepository, never()).save(any(OtpCode.class));
        verify(emailService, never()).sendOtpEmail(anyString(), anyString());
    }
}
