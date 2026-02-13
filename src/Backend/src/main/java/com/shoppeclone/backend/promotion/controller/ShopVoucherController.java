package com.shoppeclone.backend.promotion.controller;

import com.shoppeclone.backend.promotion.entity.ShopVoucher;
import com.shoppeclone.backend.promotion.service.ShopVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shop-vouchers")
@RequiredArgsConstructor
public class ShopVoucherController {

    private final ShopVoucherService shopVoucherService;

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<ShopVoucher>> getVouchersByShop(@PathVariable String shopId) {
        return ResponseEntity.ok(shopVoucherService.getVouchersByShop(shopId));
    }

    @PostMapping
    public ResponseEntity<ShopVoucher> createShopVoucher(@RequestBody ShopVoucher shopVoucher) {
        return ResponseEntity.ok(shopVoucherService.createShopVoucher(shopVoucher));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShopVoucher> updateShopVoucher(@PathVariable String id,
            @RequestBody ShopVoucher shopVoucher) {
        return ResponseEntity.ok(shopVoucherService.updateShopVoucher(id, shopVoucher));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ShopVoucher> getVoucherByCode(@PathVariable String code) {
        return ResponseEntity.ok(shopVoucherService.getVoucherByCode(code));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShopVoucher(@PathVariable String id) {
        shopVoucherService.deleteShopVoucher(id);
        return ResponseEntity.ok().build();
    }
}
