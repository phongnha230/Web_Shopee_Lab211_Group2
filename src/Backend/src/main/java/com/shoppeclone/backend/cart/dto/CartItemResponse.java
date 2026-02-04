package com.shoppeclone.backend.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemResponse {
    private String variantId;
    private String productId;
    private String productName;
    private String productImage;
    private String variantName; // Combined color and size, e.g., "Red, XL"
    private BigDecimal price;
    private int quantity;
    private int stock;
    private BigDecimal totalPrice; // price * quantity
}
