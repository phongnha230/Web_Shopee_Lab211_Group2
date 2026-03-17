package com.shoppeclone.backend.product.repository;

import com.shoppeclone.backend.product.entity.ProductVariant;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

public interface ProductVariantRepository extends MongoRepository<ProductVariant, String> {
    List<ProductVariant> findByProductId(String productId);

    void deleteByProductId(String productId);

    // Color suggestions matching keyword (non-null, case-insensitive)
    @Query(value = "{ '$and': [ { 'color': { '$ne': null } }, { 'color': { '$regex': ?0, '$options': 'i' } } ] }",
           fields = "{ 'color': 1 }")
    List<ProductVariant> findByColorRegex(String colorRegex);

    // Size suggestions matching keyword (non-null, case-insensitive)
    @Query(value = "{ '$and': [ { 'size': { '$ne': null } }, { 'size': { '$regex': ?0, '$options': 'i' } } ] }",
           fields = "{ 'size': 1 }")
    List<ProductVariant> findBySizeRegex(String sizeRegex);
}
