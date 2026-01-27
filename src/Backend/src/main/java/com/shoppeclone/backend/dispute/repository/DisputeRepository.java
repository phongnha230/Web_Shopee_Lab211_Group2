package com.shoppeclone.backend.dispute.repository;

import com.shoppeclone.backend.dispute.entity.Dispute;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DisputeRepository extends MongoRepository<Dispute, String> {
    Optional<Dispute> findByOrderId(String orderId);

    List<Dispute> findByBuyerId(String buyerId);

    List<Dispute> findBySellerId(String sellerId);
}
