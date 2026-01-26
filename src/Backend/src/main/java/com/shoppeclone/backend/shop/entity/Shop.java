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
    
    // Status: ACTIVE, BANNED
    private ShopStatus status = ShopStatus.ACTIVE;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
