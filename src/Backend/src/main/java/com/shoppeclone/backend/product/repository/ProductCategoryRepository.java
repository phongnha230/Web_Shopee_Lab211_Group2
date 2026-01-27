package com.shoppeclone.backend.product.repository;

import com.shoppeclone.backend.product.entity.ProductCategory;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ProductCategoryRepository extends MongoRepository<ProductCategory, String> {
    List<ProductCategory> findByProductId(String productId);

    List<ProductCategory> findByCategoryId(String categoryId);

    void deleteByProductId(String productId);

    void deleteByProductIdAndCategoryId(String productId, String categoryId);
}
