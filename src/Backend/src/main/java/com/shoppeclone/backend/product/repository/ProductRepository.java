package com.shoppeclone.backend.product.repository;

import com.shoppeclone.backend.product.entity.Product;
import com.shoppeclone.backend.product.entity.ProductStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByShopId(String shopId);

    List<Product> findByStatus(ProductStatus status);

    List<Product> findByShopIdAndStatus(String shopId, ProductStatus status);
}
