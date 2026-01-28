package com.shoppeclone.backend.cart.repository;

import com.shoppeclone.backend.cart.entity.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface CartRepository extends MongoRepository<Cart, String> {
    Optional<Cart> findByUserId(String userId);
}
