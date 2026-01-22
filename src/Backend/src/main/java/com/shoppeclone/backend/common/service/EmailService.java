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
        message.setSubject("Mã xác thực ShoppeClone");
        message.setText(
                "Xin chào,\n\n" +
                        "Mã OTP của bạn là: " + otpCode + "\n\n" +
                        "Mã này có hiệu lực trong 5 phút.\n\n" +
                        "Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email.\n\n" +
                        "Trân trọng,\n" +
                        "ShoppeClone Team");

        mailSender.send(message);
    }
}