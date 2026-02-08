package com.shoppeclone.backend.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDisplayDto {
    private String variantId;
    private String productId;
    private String productName;
    private String productImage;
    private String variantName;
    private int quantity;
    private BigDecimal price;
}
