package com.shoppeclone.backend.review.repository;

import com.shoppeclone.backend.review.entity.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByProductId(String productId);

    List<Review> findByUserId(String userId);

    boolean existsByUserIdAndProductIdAndOrderId(String userId, String productId, String orderId);

    List<Review> findByOrderId(String orderId);

    List<Review> findByUserIdAndOrderId(String userId, String orderId);

    List<Review> findByShopId(String shopId);
}
