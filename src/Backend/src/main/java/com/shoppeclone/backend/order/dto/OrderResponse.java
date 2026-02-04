package com.shoppeclone.backend.order.dto;

import com.shoppeclone.backend.order.entity.Order;
import com.shoppeclone.backend.order.entity.OrderItem;
import com.shoppeclone.backend.order.entity.OrderShipping;
import com.shoppeclone.backend.order.entity.OrderStatus;
import com.shoppeclone.backend.order.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String id;
    private String userId;
    private BigDecimal totalPrice;
    private BigDecimal discount;
    private String voucherCode;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private List<OrderItem> items;
    private OrderShipping shipping;

    // Shipper-related fields
    private String shipperId;
    private LocalDateTime assignedAt;
    private String deliveryNote;
    private String proofOfDeliveryUrl;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private String cancelReason;

    // Review-related fields
    @Builder.Default
    private List<OrderItemReviewInfo> itemReviewInfo = new ArrayList<>();
    private boolean canReviewAny;

    public static OrderResponse fromOrder(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .totalPrice(order.getTotalPrice())
                .discount(order.getDiscount())
                .voucherCode(order.getVoucherCode())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .items(order.getItems())
                .shipping(order.getShipping())
                .shipperId(order.getShipperId())
                .assignedAt(order.getAssignedAt())
                .deliveryNote(order.getDeliveryNote())
                .proofOfDeliveryUrl(order.getProofOfDeliveryUrl())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .shippedAt(order.getShippedAt())
                .completedAt(order.getCompletedAt())
                .cancelledAt(order.getCancelledAt())
                .cancelReason(order.getCancelReason())
                .itemReviewInfo(new ArrayList<>())
                .canReviewAny(false)
                .build();
    }
}
