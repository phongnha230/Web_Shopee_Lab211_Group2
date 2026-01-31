package com.shoppeclone.backend.order.service;

import com.shoppeclone.backend.order.dto.OrderRequest;
import com.shoppeclone.backend.order.entity.Order;
import com.shoppeclone.backend.order.entity.OrderStatus;

import java.util.List;

public interface OrderService {
    Order createOrder(String userId, OrderRequest request);

    Order getOrder(String orderId);

    List<Order> getUserOrders(String userId);

    Order updateOrderStatus(String orderId, OrderStatus status);

    Order cancelOrder(String orderId);

    Order updateShipment(String orderId, String trackingCode, String providerId);

    Order assignShipper(String orderId, String shipperId);
}
