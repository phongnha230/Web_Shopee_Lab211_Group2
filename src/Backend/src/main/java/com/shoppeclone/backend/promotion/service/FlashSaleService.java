package com.shoppeclone.backend.promotion.service;

import com.shoppeclone.backend.promotion.entity.FlashSale;
import com.shoppeclone.backend.promotion.entity.FlashSaleItem;

import java.util.List;

public interface FlashSaleService {
    FlashSale createFlashSale(FlashSale flashSale);

    FlashSaleItem addItemToFlashSale(String flashSaleId, FlashSaleItem item);

    List<FlashSale> getActiveFlashSales();

    List<FlashSaleItem> getItemsByFlashSaleId(String flashSaleId);
}
