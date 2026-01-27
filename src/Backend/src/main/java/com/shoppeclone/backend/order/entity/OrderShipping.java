package com.shoppeclone.backend.order.entity;

import com.shoppeclone.backend.user.model.Address;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderShipping {
    private String providerId; // FK to shipping_providers
    private String trackingCode;
    private String status; // WAITING, PICKED, DELIVERING, DELIVERED
    private Address address;
    private BigDecimal shippingFee;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
}
