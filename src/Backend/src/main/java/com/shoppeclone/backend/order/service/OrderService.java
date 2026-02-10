package com.shoppeclone.backend.order.service;

import com.shoppeclone.backend.order.dto.OrderRequest;
import com.shoppeclone.backend.order.entity.Order;
import com.shoppeclone.backend.order.entity.OrderStatus;

import java.util.List;

public interface OrderService {
    Order createOrder(String userId, OrderRequest request);

    Order getOrder(String orderId);

    List<Order> getUserOrders(String userId);

    // Get orders by shop ID (for seller)
    List<Order> getOrdersByShopId(String shopId, OrderStatus status);

    Order updateOrderStatus(String orderId, OrderStatus status);

    Order cancelOrder(String orderId);

    Order updateShipment(String orderId, String trackingCode, String providerId);

    /** Delete all orders for a user (e.g. clear virtual/test shop data). Restores stock for non-cancelled orders. */
    void deleteAllUserOrders(String userId);
}
