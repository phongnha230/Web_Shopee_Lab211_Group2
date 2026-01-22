package com.shoppeclone.backend.auth.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import java.time.LocalDateTime;

@Document(collection = "otp_codes")
@Data
public class OtpCode {
    @Id
    private String id;

    @DBRef
    private User user;

    private String code; // Mã OTP 6 số
    private String type; // EMAIL_VERIFICATION, PASSWORD_RESET
    private LocalDateTime expiresAt;
    private boolean used = false;
    private LocalDateTime createdAt;
}