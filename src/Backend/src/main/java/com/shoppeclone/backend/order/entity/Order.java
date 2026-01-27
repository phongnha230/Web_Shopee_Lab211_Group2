package com.shoppeclone.backend.order.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "orders")
@Data
public class Order {
    @Id
    private String id;

    @Indexed
    private String userId;

    private BigDecimal totalPrice;
    private BigDecimal discount;
    private String voucherCode; // Applied voucher code

    private OrderStatus orderStatus = OrderStatus.PENDING;
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    private List<OrderItem> items = new ArrayList<>();
    private OrderShipping shipping;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
