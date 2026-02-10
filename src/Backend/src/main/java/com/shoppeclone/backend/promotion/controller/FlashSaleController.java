package com.shoppeclone.backend.promotion.controller;

import com.shoppeclone.backend.promotion.entity.FlashSale;
import com.shoppeclone.backend.promotion.entity.FlashSaleItem;
import com.shoppeclone.backend.promotion.service.FlashSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flash-sales")
@RequiredArgsConstructor
public class FlashSaleController {

    private final FlashSaleService flashSaleService;

    @GetMapping("/active")
    public ResponseEntity<List<FlashSale>> getActiveFlashSales() {
        return ResponseEntity.ok(flashSaleService.getActiveFlashSales());
    }

    /** Trả về flash sale đang chạy (có startTime, endTime) để trang chủ hiển thị countdown thật. */
    @GetMapping("/current")
    public ResponseEntity<FlashSale> getCurrentFlashSale() {
        return flashSaleService.getCurrentFlashSale()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<FlashSale> createFlashSale(@RequestBody FlashSale flashSale) {
        return ResponseEntity.ok(flashSaleService.createFlashSale(flashSale));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<FlashSale> updateFlashSale(@PathVariable String id, @RequestBody FlashSale flashSale) {
        return ResponseEntity.ok(flashSaleService.updateFlashSale(id, flashSale));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlashSale(@PathVariable String id) {
        flashSaleService.deleteFlashSale(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{flashSaleId}/items")
    public ResponseEntity<List<FlashSaleItem>> getFlashSaleItems(@PathVariable String flashSaleId) {
        return ResponseEntity.ok(flashSaleService.getItemsByFlashSaleId(flashSaleId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{flashSaleId}/items")
    public ResponseEntity<FlashSaleItem> addFlashSaleItem(@PathVariable String flashSaleId,
            @RequestBody FlashSaleItem item) {
        return ResponseEntity.ok(flashSaleService.addItemToFlashSale(flashSaleId, item));
    }
}
