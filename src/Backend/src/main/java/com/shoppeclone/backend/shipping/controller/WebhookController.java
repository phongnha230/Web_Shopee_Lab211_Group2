package com.shoppeclone.backend.shipping.controller;

import com.shoppeclone.backend.order.entity.Order;
import com.shoppeclone.backend.order.entity.OrderStatus;
import com.shoppeclone.backend.order.entity.PaymentStatus;
import com.shoppeclone.backend.order.repository.OrderRepository;
import com.shoppeclone.backend.order.service.OrderService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks/shipping")
@RequiredArgsConstructor
public class WebhookController {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Data
    public static class ShippingUpdatePayload {
        private String trackingCode;
        private String status; // PICKED, DELIVERING, DELIVERED, RETURNED
        private String location;
        private String reason; // Reason for failed shipping / rejected delivery
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> receiveShippingUpdate(@RequestBody ShippingUpdatePayload payload) {
        LocalDateTime eventTime = LocalDateTime.now();

        Order order = orderRepository.findByTrackingCode(payload.getTrackingCode())
                .orElseThrow(() -> new RuntimeException("Tracking code not found: " + payload.getTrackingCode()));

        if (order.getShipping() != null) {
            order.getShipping().setStatus(payload.getStatus());

            if ("DELIVERED".equalsIgnoreCase(payload.getStatus())) {
                order = orderService.updateOrderStatus(order.getId(), OrderStatus.COMPLETED);
                if (order.getShipping() != null) {
                    order.getShipping().setStatus(payload.getStatus());
                    if (order.getShipping().getDeliveredAt() == null) {
                        order.getShipping().setDeliveredAt(eventTime);
                    }
                }
                order.setPaymentStatus(PaymentStatus.PAID); // COD assumption
            } else if ("RETURNED".equalsIgnoreCase(payload.getStatus())) {
                String reason = (payload.getReason() != null && !payload.getReason().isBlank())
                        ? payload.getReason().trim()
                        : "Order was rejected or could not be delivered";
                order = orderService.markOrderAsDeliveryFailed(order.getId(), reason);
                if (order.getShipping() != null) {
                    order.getShipping().setStatus(payload.getStatus());
                }
            }
        }

        order.setUpdatedAt(eventTime);
        orderRepository.save(order);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("orderId", order.getId());
        response.put("trackingCode", order.getShipping() != null ? order.getShipping().getTrackingCode() : null);
        response.put("orderStatus", order.getOrderStatus());
        response.put("shippingStatus", payload.getStatus());
        response.put("cancelReason", order.getCancelReason());
        response.put("message", "Shipping status updated successfully");
        response.put("updatedAt", order.getUpdatedAt());

        return ResponseEntity.ok(response);
    }
}

