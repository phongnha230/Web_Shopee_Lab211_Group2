package com.shoppeclone.backend.shop.repository;

import com.shoppeclone.backend.shop.entity.Shop;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface ShopRepository extends MongoRepository<Shop, String> {
    Optional<Shop> findBySellerId(String sellerId);

    List<Shop> findByStatus(com.shoppeclone.backend.shop.entity.ShopStatus status);

    boolean existsBySellerId(String sellerId);
}
