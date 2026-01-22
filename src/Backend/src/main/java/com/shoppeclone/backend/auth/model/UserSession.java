package com.shoppeclone.backend.auth.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import java.time.LocalDateTime;

@Document(collection = "user_sessions")
@Data
public class UserSession {
    @Id
    private String id;
    
    @DBRef
    private User user;
    
    private String refreshToken;
    private LocalDateTime expiresAt;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
}