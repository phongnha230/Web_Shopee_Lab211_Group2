package com.shoppeclone.backend.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemResponse {
    private String variantId;
    private String productId;
    /** Category IDs this product belongs to (for voucher validation) */
    private List<String> categoryIds;
    private String productName;
    private String productImage;
    private String variantName; // Combined color and size, e.g., "Red, XL"
    private BigDecimal price;
    private int quantity;
    private int stock;
    private BigDecimal totalPrice; // price * quantity
}
