package com.shoppeclone.backend.shop.controller;

import com.shoppeclone.backend.shop.dto.ShopRegisterRequest;
import com.shoppeclone.backend.shop.entity.Shop;
import com.shoppeclone.backend.shop.service.ShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ShopController {

    private final ShopService shopService;

    private String getEmail(Authentication authentication) {
        return authentication.getName();
    }

    @PostMapping("/register")
    public ResponseEntity<Shop> registerShop(
            @Valid @RequestBody ShopRegisterRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(shopService.registerShop(getEmail(authentication), request));
    }

    @GetMapping("/my-shop")
    public ResponseEntity<Shop> getMyShop(Authentication authentication) {
        return ResponseEntity.ok(shopService.getMyShop(getEmail(authentication)));
    }

    // ADMIN ONLY
    @GetMapping("/admin/pending")
    // @PreAuthorize("hasRole('ADMIN')") // Uncomment if using Method Security
    public ResponseEntity<List<Shop>> getPendingShops() {
        return ResponseEntity.ok(shopService.getPendingShops());
    }

    @PostMapping("/admin/approve/{shopId}")
    public ResponseEntity<String> approveShop(@PathVariable String shopId) {
        shopService.approveShop(shopId);
        return ResponseEntity.ok("Shop approved and User promoted to Seller!");
    }

    @PostMapping("/admin/reject/{shopId}")
    public ResponseEntity<String> rejectShop(@PathVariable String shopId,
            @RequestParam(required = false) String reason) {
        shopService.rejectShop(shopId, reason);
        return ResponseEntity.ok("Shop rejected.");
    }
}
