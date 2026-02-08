package com.shoppeclone.backend.order.dto;

import lombok.Data;

@Data
public class OrderItemRequest {
    private String variantId;
    private int quantity;
    private java.math.BigDecimal price;
}
