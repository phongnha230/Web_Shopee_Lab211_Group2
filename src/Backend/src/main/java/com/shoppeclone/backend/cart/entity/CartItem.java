package com.shoppeclone.backend.cart.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CartItem {
    private String variantId;
    private int quantity;
    private LocalDateTime addedAt = LocalDateTime.now();
}
