package com.shoppeclone.backend.order.repository;

import com.shoppeclone.backend.order.entity.Order;
import com.shoppeclone.backend.order.entity.OrderStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByUserId(String userId);

    // Query orders by shop ID (for seller)
    List<Order> findByShopId(String shopId);

    // Query orders by shop ID and status
    List<Order> findByShopIdAndOrderStatus(String shopId, com.shoppeclone.backend.order.entity.OrderStatus orderStatus);

    List<Order> findByUserIdAndOrderStatus(String userId, OrderStatus orderStatus);

    // Support nested query for shipping tracking code
    @org.springframework.data.mongodb.repository.Query("{'shipping.trackingCode': ?0}")
    java.util.Optional<Order> findByTrackingCode(String trackingCode);

    void deleteByUserId(String userId);

}
