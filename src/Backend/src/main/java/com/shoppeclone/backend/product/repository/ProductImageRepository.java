package com.shoppeclone.backend.product.repository;

import com.shoppeclone.backend.product.entity.ProductImage;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ProductImageRepository extends MongoRepository<ProductImage, String> {
    List<ProductImage> findByProductIdOrderByDisplayOrderAsc(String productId);

    void deleteByProductId(String productId);
}
