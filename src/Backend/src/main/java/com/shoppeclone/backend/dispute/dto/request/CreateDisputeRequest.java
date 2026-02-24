package com.shoppeclone.backend.dispute.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateDisputeRequest {
    @NotBlank(message = "orderId is required")
    private String orderId;

    @NotBlank(message = "reason is required")
    private String reason;

    @NotBlank(message = "description is required")
    private String description;
}
