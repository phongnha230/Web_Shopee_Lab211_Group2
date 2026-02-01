package com.shoppeclone.backend.shop.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "shops")
@Data
public class Shop {
    @Id
    private String id;

    @Indexed(unique = true)
    private String ownerId; // Link to User ID

    private String name;
    private String description;
    private String address;
    private String phone;
    private String email; // Shop Email
    private String pickupAddress;
    private String logo;

    // Identity Info
    private String identityIdCard; // 12-digit ID Card Number
    private String idCardFront; // URL/Path
    private String idCardBack; // URL/Path

    // Bank Info
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountHolder;

    private ShopStatus status;
    private String rejectionReason;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
