package com.shoppeclone.backend.auth.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import java.time.LocalDateTime;

@Document(collection = "password_reset_tokens")
@Data
public class PasswordResetToken {
    @Id
    private String id;

    @DBRef
    private User user;

    private String token; // UUID token
    private LocalDateTime expiresAt;
    private boolean used = false;
    private LocalDateTime createdAt;
}
