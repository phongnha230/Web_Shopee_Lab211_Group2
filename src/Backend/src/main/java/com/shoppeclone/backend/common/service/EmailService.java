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

    public void sendShopApprovalEmail(String to, String shopName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Shop Application Approved - ShoppeClone");
        message.setText(
                "Hello,\n\n" +
                        "Congratulations! Your shop application for '" + shopName + "' has been APPROVED.\n\n" +
                        "You can now access your Shop Dashboard and start listing products.\n\n" +
                        "Login here: http://localhost:3000/login\n\n" +
                        "Best regards,\n" +
                        "ShoppeClone Admin Team");

        mailSender.send(message);
        System.out.println("✅ Shop Approval email sent to: " + to);
    }

    public void sendShopRejectionEmail(String to, String shopName, String reason) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Shop Application Rejected - ShoppeClone");
        message.setText(
                "Hello,\n\n" +
                        "We regret to inform you that your shop application for '" + shopName
                        + "' has been REJECTED.\n\n" +
                        "Reason: " + reason + "\n\n" +
                        "You can contact us for more details or submit a new application after addressing the issues.\n\n"
                        +
                        "Best regards,\n" +
                        "ShoppeClone Admin Team");

        mailSender.send(message);
        System.out.println("❌ Shop Rejection email sent to: " + to);
    }

    public void sendWelcomeEmail(String to, String name) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Welcome to ShoppeClone! 🎉");
        message.setText(
                "Hello " + name + ",\n\n" +
                        "Welcome to ShoppeClone! Your account has been successfully verified.\n\n" +
                        "You can now log in and start shopping with us.\n" +
                        "We hope you have a great experience!\n\n" +
                        "Best regards,\n" +
                        "ShoppeClone Team");

        mailSender.send(message);
        System.out.println("🎉 Welcome email sent to: " + to);
    }
}