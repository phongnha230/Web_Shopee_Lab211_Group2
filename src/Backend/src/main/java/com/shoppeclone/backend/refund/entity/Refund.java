package com.shoppeclone.backend.refund.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "refunds")
@Data
public class Refund {
    @Id
    private String id;

    private String orderId;
    private String buyerId;

    private BigDecimal amount;
    private String reason;

    private RefundStatus status = RefundStatus.REQUESTED;

    private String approvedBy; // adminId
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;

    public Refund() {
        this.createdAt = LocalDateTime.now();
    }
}
