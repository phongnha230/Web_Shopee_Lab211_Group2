package com.shoppeclone.backend.follow.repository;

import com.shoppeclone.backend.follow.entity.Follow;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowRepository extends MongoRepository<Follow, String> {
    Optional<Follow> findByUserIdAndShopId(String userId, String shopId);

    long countByShopId(String shopId);

    boolean existsByUserIdAndShopId(String userId, String shopId);

    java.util.List<Follow> findByShopId(String shopId);
}
