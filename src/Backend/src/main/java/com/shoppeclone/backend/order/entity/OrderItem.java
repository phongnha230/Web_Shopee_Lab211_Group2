package com.shoppeclone.backend.order.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItem {
    private String variantId;
    private BigDecimal price;
    private int quantity;
    private String flashSaleId; // If item was bought during flash sale
}
