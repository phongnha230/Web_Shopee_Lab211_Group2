package com.shoppeclone.backend.shipper.service.impl;

import com.shoppeclone.backend.order.entity.Order;
import com.shoppeclone.backend.order.entity.OrderItem;
import com.shoppeclone.backend.order.entity.OrderStatus;
import com.shoppeclone.backend.order.entity.PaymentStatus;
import com.shoppeclone.backend.order.repository.OrderRepository;
import com.shoppeclone.backend.product.entity.ProductVariant;
import com.shoppeclone.backend.product.repository.ProductVariantRepository;
import com.shoppeclone.backend.shipper.dto.DeliveryUpdateRequest;
import com.shoppeclone.backend.shipper.service.ShipperService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShipperServiceImpl implements ShipperService {

    private final OrderRepository orderRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    public List<Order> getAssignedOrders(String shipperId) {
        return orderRepository.findByShipperId(shipperId);
    }

    @Override
    @Transactional
    public Order pickupOrder(String shipperId, String orderId) {
        Order order = validateShipperOwnsOrder(shipperId, orderId);

        // Update status to SHIPPING (picked up from shop)
        order.setOrderStatus(OrderStatus.SHIPPING);
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order markAsShipping(String shipperId, String orderId) {
        Order order = validateShipperOwnsOrder(shipperId, orderId);

        // Mark as shipped (in transit)
        order.setOrderStatus(OrderStatus.SHIPPED);
        if (order.getShippedAt() == null) {
            order.setShippedAt(LocalDateTime.now());
        }
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order completeDelivery(String shipperId, String orderId, DeliveryUpdateRequest request) {
        Order order = validateShipperOwnsOrder(shipperId, orderId);

        // Validate order status - must be SHIPPED before completion
        if (order.getOrderStatus() != OrderStatus.SHIPPED) {
            throw new RuntimeException(
                    "Order must be SHIPPED before completion. Current status: " + order.getOrderStatus());
        }

        // Mark as completed
        order.setOrderStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // Auto-update payment status for COD (Cash on Delivery)
        if (order.getPaymentStatus() == PaymentStatus.UNPAID) {
            order.setPaymentStatus(PaymentStatus.PAID); // Shipper collected cash
        }

        // Save delivery note and proof
        if (request != null) {
            if (request.getNote() != null) {
                order.setDeliveryNote(request.getNote());
            }
            if (request.getProofOfDeliveryUrl() != null) {
                order.setProofOfDeliveryUrl(request.getProofOfDeliveryUrl());
            }
        }

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order failDelivery(String shipperId, String orderId, DeliveryUpdateRequest request) {
        Order order = validateShipperOwnsOrder(shipperId, orderId);

        // Restore stock when delivery fails
        for (OrderItem item : order.getItems()) {
            ProductVariant variant = productVariantRepository.findById(item.getVariantId()).orElse(null);
            if (variant != null) {
                variant.setStock(variant.getStock() + item.getQuantity());
                productVariantRepository.save(variant);
            }
        }

        // Mark as failed
        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // Save failure reason
        if (request != null && request.getFailureReason() != null) {
            order.setCancelReason(request.getFailureReason());
            order.setDeliveryNote(request.getNote());
        }

        return orderRepository.save(order);
    }

    private Order validateShipperOwnsOrder(String shipperId, String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (!shipperId.equals(order.getShipperId())) {
            throw new RuntimeException("This order is not assigned to you");
        }

        return order;
    }
}
