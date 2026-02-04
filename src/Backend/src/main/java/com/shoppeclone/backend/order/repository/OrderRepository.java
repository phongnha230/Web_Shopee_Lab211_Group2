package com.shoppeclone.backend.order.repository;

import com.shoppeclone.backend.order.entity.Order;
import com.shoppeclone.backend.order.entity.OrderStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByUserId(String userId);

    List<Order> findByShipperId(String shipperId);

    List<Order> findByUserIdAndOrderStatus(String userId, OrderStatus orderStatus);

    // Support nested query for shipping tracking code
    @org.springframework.data.mongodb.repository.Query("{'shipping.trackingCode': ?0}")
    java.util.Optional<Order> findByTrackingCode(String trackingCode);
}
