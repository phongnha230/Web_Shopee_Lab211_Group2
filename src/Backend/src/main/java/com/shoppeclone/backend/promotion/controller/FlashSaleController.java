package com.shoppeclone.backend.promotion.controller;

import com.shoppeclone.backend.promotion.entity.FlashSale;
import com.shoppeclone.backend.promotion.entity.FlashSaleItem;
import com.shoppeclone.backend.promotion.service.FlashSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseEntity<FlashSale> createFlashSale(@RequestBody FlashSale flashSale) {
        return ResponseEntity.ok(flashSaleService.createFlashSale(flashSale));
    }

    @GetMapping("/{flashSaleId}/items")
    public ResponseEntity<List<FlashSaleItem>> getFlashSaleItems(@PathVariable String flashSaleId) {
        return ResponseEntity.ok(flashSaleService.getItemsByFlashSaleId(flashSaleId));
    }

    @PostMapping("/{flashSaleId}/items")
    public ResponseEntity<FlashSaleItem> addFlashSaleItem(@PathVariable String flashSaleId,
            @RequestBody FlashSaleItem item) {
        return ResponseEntity.ok(flashSaleService.addItemToFlashSale(flashSaleId, item));
    }
}
