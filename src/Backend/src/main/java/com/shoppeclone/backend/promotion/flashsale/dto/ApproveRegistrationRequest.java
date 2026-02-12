package com.shoppeclone.backend.promotion.flashsale.dto;

import lombok.Data;

@Data
public class ApproveRegistrationRequest {
    private String status; // APPROVED, REJECTED
    private String adminNote;
}
