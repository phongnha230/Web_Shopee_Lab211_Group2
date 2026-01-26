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
    private ShopStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
