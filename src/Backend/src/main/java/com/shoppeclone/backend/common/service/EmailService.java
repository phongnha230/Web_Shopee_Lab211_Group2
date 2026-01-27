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

    public void sendLoginAlert(String to, String time, String ip, String device) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Security Alert: New Login Detected");
        message.setText(
                "Hello,\n\n" +
                        "We detected a new login to your ShoppeClone account.\n\n" +
                        "Time: " + time + "\n" +
                        "IP Address: " + ip + "\n" +
                        "Device: " + device + "\n\n" +
                        "If this was you, you can ignore this email.\n" +
                        "If you did not log in, please change your password immediately.\n\n" +
                        "Best regards,\n" +
                        "ShoppeClone Team");

        mailSender.send(message);
        System.out.println("⚠️ Login alert email sent to: " + to);
    }
}