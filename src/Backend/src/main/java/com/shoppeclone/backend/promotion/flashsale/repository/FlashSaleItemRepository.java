package com.shoppeclone.backend.promotion.flashsale.repository;

import com.shoppeclone.backend.promotion.flashsale.entity.FlashSaleItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlashSaleItemRepository extends MongoRepository<FlashSaleItem, String> {
    List<FlashSaleItem> findByFlashSaleId(String flashSaleId);

    List<FlashSaleItem> findByShopId(String shopId);

    List<FlashSaleItem> findByStatus(String status);

    List<FlashSaleItem> findByFlashSaleIdAndStatus(String flashSaleId, String status);

    void deleteByFlashSaleId(String flashSaleId);

    long countByStatus(String status);

    List<FlashSaleItem> findByProductIdAndStatus(String productId, String status);

    List<FlashSaleItem> findByVariantIdAndStatus(String variantId, String status);
}
