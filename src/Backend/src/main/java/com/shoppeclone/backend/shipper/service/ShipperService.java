package com.shoppeclone.backend.shipper.service;

import com.shoppeclone.backend.order.entity.Order;
import com.shoppeclone.backend.shipper.dto.DeliveryUpdateRequest;

import java.util.List;

public interface ShipperService {
    /**
     * Get all orders assigned to a specific shipper
     */
    List<Order> getAssignedOrders(String shipperId);

    /**
     * Shipper picks up order from shop
     */
    Order pickupOrder(String shipperId, String orderId);

    /**
     * Shipper marks order as in transit
     */
    Order markAsShipping(String shipperId, String orderId);

    /**
     * Shipper completes delivery
     */
    Order completeDelivery(String shipperId, String orderId, DeliveryUpdateRequest request);

    /**
     * Shipper marks delivery as failed
     */
    Order failDelivery(String shipperId, String orderId, DeliveryUpdateRequest request);
}
