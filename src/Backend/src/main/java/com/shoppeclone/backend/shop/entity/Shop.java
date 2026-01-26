package com.shoppeclone.backend.shop.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;

@Document(collection = "shops")
@Data
public class Shop {
    @Id
    private String id;

    @Indexed
    private String sellerId; // FK to seller_profiles.id (or users.id)

    private String name;
    private Double rating = 0.0;

    // Pickup Address (for Shipper to collect products)
    private String province; // Tỉnh/Thành phố
    private String district; // Quận/Huyện
    private String ward; // Phường/Xã
    private String address; // Địa chỉ chi tiết (số nhà, tên đường)

    // Contact Information
    private String phone; // Số điện thoại shop
    private String email; // Email shop

    // Status: ACTIVE, BANNED
    private ShopStatus status = ShopStatus.ACTIVE;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
