package com.shoppeclone.backend.shipping.controller;

import com.shoppeclone.backend.order.entity.Order;
import com.shoppeclone.backend.order.entity.OrderStatus;
import com.shoppeclone.backend.order.repository.OrderRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/webhooks/shipping")
@RequiredArgsConstructor
public class WebhookController {

    private final OrderRepository orderRepository;

    @Data
    public static class ShippingUpdatePayload {
        private String trackingCode;
        private String status; // PICKED, DELIVERING, DELIVERED, RETURNED
        private String location;
        private String reason; // Lý do khi vận chuyển thất bại (RETURNED)
    }

    @PostMapping("/update")
    public ResponseEntity<String> receiveShippingUpdate(@RequestBody ShippingUpdatePayload payload) {
        // 1. Find Order by Tracking Code
        Order order = orderRepository.findByTrackingCode(payload.getTrackingCode())
                .orElseThrow(() -> new RuntimeException("Tracking code not found: " + payload.getTrackingCode()));

        // 2. Update Shipping Status in Order
        // Map carrier status to our OrderStatus
        if (order.getShipping() != null) {
            order.getShipping().setStatus(payload.getStatus());

            if ("DELIVERED".equalsIgnoreCase(payload.getStatus())) {
                order.setOrderStatus(OrderStatus.COMPLETED);
                order.getShipping().setDeliveredAt(LocalDateTime.now());
                if (order.getCompletedAt() == null) {
                    order.setCompletedAt(LocalDateTime.now());
                }
                order.setPaymentStatus(com.shoppeclone.backend.order.entity.PaymentStatus.PAID); // COD assumption
            } else if ("RETURNED".equalsIgnoreCase(payload.getStatus())) {
                order.setOrderStatus(OrderStatus.CANCELLED);
                order.setCancelledAt(LocalDateTime.now());
                String reason = (payload.getReason() != null && !payload.getReason().isBlank())
                        ? payload.getReason().trim()
                        : "Đơn hàng từ chối nhận / không giao được hàng";
                order.setCancelReason(reason);
            }
        }

        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        return ResponseEntity.ok("Received");
    }
}
