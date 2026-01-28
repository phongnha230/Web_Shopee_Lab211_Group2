package com.shoppeclone.backend.refund.repository;

import com.shoppeclone.backend.refund.entity.Refund;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends MongoRepository<Refund, String> {
    Optional<Refund> findByOrderId(String orderId);

    List<Refund> findByBuyerId(String buyerId);
}
