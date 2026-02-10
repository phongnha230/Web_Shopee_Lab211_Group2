package com.shoppeclone.backend.product.repository;

import com.shoppeclone.backend.product.entity.Product;
import com.shoppeclone.backend.product.entity.ProductStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByShopId(String shopId);

    List<Product> findByStatus(ProductStatus status);

    long countByShopId(String shopId);

    // Fetch top 5 items by sold count descending
    List<Product> findTop5ByOrderBySoldDesc();

    List<Product> findByIsFlashSaleTrue();

    // Simple text search by name or description (case-insensitive)
    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String nameKeyword,
            String descriptionKeyword);
}
