package com.shoppeclone.backend.dispute.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "disputes")
@Data
public class Dispute {
    @Id
    private String id;

    private String orderId;
    private String buyerId;
    private String sellerId;

    private String reason; // Enum or String as per requirement, using String for flexibility
    private String description;

    private DisputeStatus status = DisputeStatus.OPEN;

    private String adminNote;

    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public Dispute() {
        this.createdAt = LocalDateTime.now();
    }
}
