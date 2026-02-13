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
@lombok.Getter
@lombok.Setter
@lombok.ToString
public class Order {
    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String shopId; // Shop that owns this order

    private BigDecimal totalPrice;
    private BigDecimal discount;
    private String voucherCode; // Applied product voucher code
    private String shippingVoucherCode; // Applied shipping voucher code
    private String shopVoucherCode; // Applied shop voucher code
    private BigDecimal shopDiscount; // Discount amount from shop voucher
    private BigDecimal shippingDiscount; // Discount amount for shipping

    private OrderStatus orderStatus = OrderStatus.PENDING;
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    private List<OrderItem> items = new ArrayList<>();
    private OrderShipping shipping;

    // Shipper-related fields
    private String shipperId; // ID of assigned shipper
    private LocalDateTime assignedAt; // When order was assigned to shipper
    private String deliveryNote; // Note from shipper about delivery
    private String proofOfDeliveryUrl; // Image URL of delivery proof

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private String cancelReason;

}
