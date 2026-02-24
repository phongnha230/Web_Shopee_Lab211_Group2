package com.shoppeclone.backend.refund.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RequestRefundRequest {
    @NotBlank(message = "reason is required")
    private String reason;

    @Positive(message = "amount must be > 0")
    private BigDecimal amount;
}
