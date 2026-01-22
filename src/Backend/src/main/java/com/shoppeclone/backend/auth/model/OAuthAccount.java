package com.shoppeclone.backend.auth.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import java.time.LocalDateTime;

@Document(collection = "oauth_accounts")
@Data
public class OAuthAccount {
    @Id
    private String id;

    @DBRef
    private User user;

    private String provider; // "google"
    private String providerId; // Google user ID
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}