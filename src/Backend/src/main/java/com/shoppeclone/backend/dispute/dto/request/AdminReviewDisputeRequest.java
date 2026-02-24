package com.shoppeclone.backend.dispute.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminReviewDisputeRequest {
    @NotBlank(message = "status is required")
    private String status;

    private String adminNote;

    private Boolean approveRefund;

    @Positive(message = "refundAmount must be > 0")
    private BigDecimal refundAmount;
}
