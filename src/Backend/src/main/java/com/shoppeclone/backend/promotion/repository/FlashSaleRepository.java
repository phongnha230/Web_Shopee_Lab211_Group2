package com.shoppeclone.backend.promotion.repository;

import com.shoppeclone.backend.promotion.entity.FlashSale;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlashSaleRepository extends MongoRepository<FlashSale, String> {
    List<FlashSale> findByStartTimeAfter(LocalDateTime time);
}
