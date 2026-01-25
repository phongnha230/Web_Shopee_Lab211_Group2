package com.shoppeclone.backend.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String to, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("ShoppeClone Verification Code");
        message.setText(
                "Hello,\n\n" +
                        "Your OTP code is: " + otpCode + "\n\n" +
                        "This code is valid for 2 minutes.\n\n" +
                        "If you did not request this code, please ignore this email.\n\n" +
                        "Best regards,\n" +
                        "ShoppeClone Team");

        mailSender.send(message);
    }

    public void sendPasswordResetOtp(String to, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Reset Password - ShoppeClone");
        message.setText(
                "Hello,\n\n" +
                        "You have requested to reset your password for your ShoppeClone account.\n\n" +
                        "Your OTP code is: " + otpCode + "\n\n" +
                        "This code is valid for 10 minutes.\n\n" +
                        "If you did not request a password reset, please ignore this email.\n\n" +
                        "Best regards,\n" +
                        "ShoppeClone Team");

        mailSender.send(message);
        System.out.println("✅ Password reset OTP email sent to: " + to);
    }
}