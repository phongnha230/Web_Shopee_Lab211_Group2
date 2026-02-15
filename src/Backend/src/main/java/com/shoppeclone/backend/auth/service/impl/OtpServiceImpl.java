package com.shoppeclone.backend.auth.service.impl;

import com.shoppeclone.backend.auth.model.OtpCode;
import com.shoppeclone.backend.auth.model.User;
import com.shoppeclone.backend.auth.repository.OtpCodeRepository;
import com.shoppeclone.backend.auth.repository.UserRepository;
import com.shoppeclone.backend.auth.service.OtpService;
import com.shoppeclone.backend.common.service.EmailService;
import com.shoppeclone.backend.user.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final UserRepository userRepository;
    private final OtpCodeRepository otpCodeRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    @Value("${otp.expiration}")
    private Long otpExpiration;

    @Override
    public void sendOtpEmail(String email) {
        System.out.println("🔍 SEND OTP - Email: " + email);

        // Tìm user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Xóa OTP cũ nếu có
        otpCodeRepository.deleteByUser(user);
        System.out.println("🗑️ Old OTPs deleted");

        // Tạo mã OTP 6 số
        String otpCode = generateOtpCode();
        System.out.println("✅ GENERATED OTP: " + otpCode);

        // Lưu vào database
        OtpCode otp = new OtpCode();
        otp.setUser(user);
        otp.setCode(otpCode);
        otp.setType("EMAIL_VERIFICATION");
        otp.setExpiresAt(LocalDateTime.now().plusSeconds(otpExpiration / 1000));
        otp.setCreatedAt(LocalDateTime.now());
        otpCodeRepository.save(otp);
        System.out.println("💾 OTP SAVED - Expires: " + otp.getExpiresAt());

        // Gửi email
        emailService.sendOtpEmail(email, otpCode);
        System.out.println("📧 EMAIL SENT");
    }

    @Override
    public void verifyOtp(String email, String code) {
        System.out.println("🔍 VERIFY OTP - Email: " + email + ", Code: " + code);

        // Tìm user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("✅ User found: " + user.getEmail());

        // Tìm OTP - THÊM DEBUG
        OtpCode otp = otpCodeRepository.findByUserAndCodeAndTypeAndUsed(
                user, code, "EMAIL_VERIFICATION", false)
                .orElseThrow(() -> {
                    System.out.println("❌ OTP NOT FOUND!");
                    System.out.println("  User: " + user.getEmail());
                    System.out.println("  Code: " + code);
                    System.out.println("  Type: EMAIL_VERIFICATION");
                    System.out.println("  Used: false");
                    return new RuntimeException("Invalid OTP code");
                });

        System.out.println("✅ OTP FOUND - Expires: " + otp.getExpiresAt());

        // Kiểm tra hết hạn
        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            System.out.println("❌ OTP EXPIRED");
            throw new RuntimeException("OTP code expired");
        }

        System.out.println("✅ OTP VALID - Marking as used");

        // Đánh dấu OTP đã sử dụng
        otp.setUsed(true);
        otpCodeRepository.save(otp);

        // Cập nhật user
        user.setEmailVerified(true);
        userRepository.save(user);

        System.out.println("🎉 EMAIL VERIFIED SUCCESS: " + user.getEmail());

        // 🔥 WELCOME LOGIC (Email + Notification)
        try {
            // 1. Send Welcome Email
            emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());

            // 2. Create Welcome Notification
            notificationService.createNotification(
                    user.getId(),
                    "Welcome to ShopeeClone! 🎉",
                    "Welcome " + user.getFullName() + "! Your account has been successfully verified. Happy shopping!",
                    "SYSTEM");

            // 3. Notify Admins
            java.util.List<User> admins = userRepository.findByRolesName("ROLE_ADMIN");
            for (User admin : admins) {
                notificationService.createNotification(
                        admin.getId(),
                        "New User Registered",
                        "New user " + user.getFullName() + " (" + user.getEmail()
                                + ") has registered and verified their account.",
                        "ADMIN_ALERT");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Failed to send welcome message: " + e.getMessage());
        }
    }

    @Override
    public void checkResetOtp(String email, String code) {
        System.out.println("🔍 CHECK RESET OTP - Email: " + email + ", Code: " + code);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Tìm OTP type PASSWORD_RESET
        OtpCode otp = otpCodeRepository.findByUserAndCodeAndTypeAndUsed(
                user, code, "PASSWORD_RESET", false)
                .orElseThrow(() -> new RuntimeException("Invalid OTP code"));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP code expired");
        }

        System.out.println("✅ RESET OTP IS VALID (Not yet used)");
    }

    private String generateOtpCode() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
