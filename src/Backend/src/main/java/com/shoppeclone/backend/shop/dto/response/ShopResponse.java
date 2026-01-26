package com.shoppeclone.backend.shop.dto.response;

import com.shoppeclone.backend.shop.entity.ShopStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShopResponse {
    private String id;
    private String sellerId;
    private String name;
    private Double rating;

    // Pickup Address (for Shipper integration)
    private String province;
    private String district;
    private String ward;
    private String address;

    // Contact Information
    private String phone;
    private String email;

    private ShopStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
