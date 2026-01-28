package com.shoppeclone.backend.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    private String id;

    @Indexed
    private String userId; // Recipient

    private String title;
    private String message;
    private String type; // SECURITY, SYSTEM, ORDER

    private boolean isRead = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}
