package com.shoppeclone.backend.shop.dto.response;

import com.shoppeclone.backend.shop.entity.ShopStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShopAdminResponse {
    private String id;
    private String ownerId;
    private String ownerFullName;
    private String ownerEmail;
    private String name;
    private String description;
    private String address;
    private String phone;
    private String email;
    private ShopStatus status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
