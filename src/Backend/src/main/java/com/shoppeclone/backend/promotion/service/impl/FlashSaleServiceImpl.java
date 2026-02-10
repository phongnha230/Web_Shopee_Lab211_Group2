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
import java.util.Optional;

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
    public FlashSale updateFlashSale(String id, FlashSale flashSale) {
        FlashSale existing = flashSaleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flash sale not found: " + id));

        // Cập nhật các trường đơn giản (ở đây chỉ có startTime, endTime)
        if (flashSale.getStartTime() != null) {
            existing.setStartTime(flashSale.getStartTime());
        }
        if (flashSale.getEndTime() != null) {
            existing.setEndTime(flashSale.getEndTime());
        }

        return flashSaleRepository.save(existing);
    }

    @Override
    public void deleteFlashSale(String id) {
        // Xoá toàn bộ items thuộc flash sale này trước
        flashSaleItemRepository.deleteByFlashSaleId(id);
        flashSaleRepository.deleteById(id);
    }

    @Override
    public FlashSaleItem addItemToFlashSale(String flashSaleId, FlashSaleItem item) {
        item.setFlashSaleId(flashSaleId);
        return flashSaleItemRepository.save(item);
    }

    @Override
    public List<FlashSale> getActiveFlashSales() {
        return flashSaleRepository.findByStartTimeAfter(LocalDateTime.now().minusDays(1));
    }

    @Override
    public Optional<FlashSale> getCurrentFlashSale() {
        LocalDateTime now = LocalDateTime.now();

        // Đơn giản: lấy tất cả, tự lọc theo thời gian hiện tại
        List<FlashSale> all = flashSaleRepository.findAll();

        return all.stream()
                .filter(fs -> fs.getStartTime() != null && fs.getEndTime() != null)
                .filter(fs -> !fs.getStartTime().isAfter(now) && !fs.getEndTime().isBefore(now))
                .findFirst();
    }

    @Override
    public List<FlashSaleItem> getItemsByFlashSaleId(String flashSaleId) {
        return flashSaleItemRepository.findByFlashSaleId(flashSaleId);
    }
}
