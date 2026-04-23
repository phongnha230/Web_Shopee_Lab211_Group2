package com.shoppeclone.backend.auth.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "rate_limits")
@Data
public class RateLimitRecord {
    @Id
    private String id;

    private String action;
    private String subjectKey;
    private int attempts;
    private LocalDateTime windowStart;
    private LocalDateTime blockedUntil;
    private LocalDateTime lastAttemptAt;

    @Indexed(expireAfterSeconds = 0)
    private LocalDateTime expireAt;
}
