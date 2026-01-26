package com.shoppeclone.backend.product.repository;

import com.shoppeclone.backend.product.entity.ProductVariant;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ProductVariantRepository extends MongoRepository<ProductVariant, String> {
    List<ProductVariant> findByProductId(String productId);

    void deleteByProductId(String productId);
}
