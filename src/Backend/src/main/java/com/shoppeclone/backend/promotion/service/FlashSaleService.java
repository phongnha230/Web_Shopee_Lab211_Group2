package com.shoppeclone.backend.promotion.service;

import com.shoppeclone.backend.promotion.entity.FlashSale;
import com.shoppeclone.backend.promotion.entity.FlashSaleItem;

import java.util.List;
import java.util.Optional;

public interface FlashSaleService {
    FlashSale createFlashSale(FlashSale flashSale);

    FlashSaleItem addItemToFlashSale(String flashSaleId, FlashSaleItem item);

    FlashSale updateFlashSale(String id, FlashSale flashSale);

    void deleteFlashSale(String id);

    List<FlashSale> getActiveFlashSales();

    /** Chiến dịch flash sale đang diễn ra (dùng cho countdown trang chủ). */
    Optional<FlashSale> getCurrentFlashSale();

    List<FlashSaleItem> getItemsByFlashSaleId(String flashSaleId);
}
