package com.shoppeclone.backend.shop.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShopRegisterRequest {
    @NotBlank(message = "Shop name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String email;

    private String description;

    // Step 2 Fields
    private String identityIdCard; // 12-digit ID Card Number
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountHolder;

    // ✅ Added to match Frontend
    private String idCardFront;
    private String idCardBack;

    // In real app, we might want file uploads for ID Card, etc.
}
