package com.shoppeclone.backend.order.entity;

public enum OrderStatus {
    PENDING, // Order created, waiting for confirmation
    CONFIRMED, // Shop confirmed the order
    PAID, // Payment completed (for online payment)
    SHIPPING, // Shipper picked up from shop, in transit
    SHIPPED, // Order is being delivered
    COMPLETED, // Order delivered successfully
    CANCELLED // Order cancelled
}
