package com.shoppeclone.backend.promotion.service.impl;

import com.shoppeclone.backend.promotion.entity.FlashSale;
import com.shoppeclone.backend.promotion.entity.FlashSaleItem;
import com.shoppeclone.backend.promotion.repository.FlashSaleItemRepository;
import com.shoppeclone.backend.promotion.repository.FlashSaleRepository;
import com.shoppeclone.backend.promotion.service.FlashSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlashSaleServiceImpl implements FlashSaleService {

    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;

    @Override
    public FlashSale createFlashSale(FlashSale flashSale) {
        return flashSaleRepository.save(flashSale);
    }

    @Override
    public FlashSaleItem addItemToFlashSale(String flashSaleId, FlashSaleItem item) {
        item.setFlashSaleId(flashSaleId);
        return flashSaleItemRepository.save(item);
    }

    @Override
    public List<FlashSale> getActiveFlashSales() {
        // Return sales that differ from now or logic as per requirement
        // For simplicity returning all starts after now or currently running logic
        // could be added
        return flashSaleRepository.findByStartTimeAfter(LocalDateTime.now().minusDays(1));
    }

    @Override
    public List<FlashSaleItem> getItemsByFlashSaleId(String flashSaleId) {
        return flashSaleItemRepository.findByFlashSaleId(flashSaleId);
    }
}
