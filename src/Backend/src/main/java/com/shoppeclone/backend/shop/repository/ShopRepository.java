package com.shoppeclone.backend.shop.repository;

import com.shoppeclone.backend.shop.entity.Shop;
import com.shoppeclone.backend.shop.entity.ShopStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepository extends MongoRepository<Shop, String> {
    Optional<Shop> findByOwnerId(String ownerId);

    boolean existsByOwnerId(String ownerId);

    List<Shop> findByStatus(ShopStatus status);
}
