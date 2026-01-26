package com.shoppeclone.backend.shop.controller;

import com.shoppeclone.backend.shop.dto.request.CreateShopRequest;
import com.shoppeclone.backend.shop.dto.request.UpdateShopRequest;
import com.shoppeclone.backend.shop.dto.response.ShopResponse;
import com.shoppeclone.backend.shop.service.ShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ShopController {

    private final ShopService shopService;

    @PostMapping
    public ResponseEntity<ShopResponse> createShop(@Valid @RequestBody CreateShopRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shopService.createShop(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShopResponse> getShopById(@PathVariable String id) {
        return ResponseEntity.ok(shopService.getShopById(id));
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<ShopResponse> getShopBySellerId(@PathVariable String sellerId) {
        return ResponseEntity.ok(shopService.getShopBySellerId(sellerId));
    }

    @GetMapping
    public ResponseEntity<List<ShopResponse>> getAllShops() {
        return ResponseEntity.ok(shopService.getAllShops());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShopResponse> updateShop(
            @PathVariable String id,
            @Valid @RequestBody UpdateShopRequest request) {
        return ResponseEntity.ok(shopService.updateShop(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShop(@PathVariable String id) {
        shopService.deleteShop(id);
        return ResponseEntity.noContent().build();
    }
}
