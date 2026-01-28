package com.shoppeclone.backend.shipping.controller;

import com.shoppeclone.backend.shipping.entity.ShippingProvider;
import com.shoppeclone.backend.shipping.service.ShippingService;
import com.shoppeclone.backend.user.model.Address;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingService shippingService;

    @GetMapping("/providers")
    public ResponseEntity<List<ShippingProvider>> getAllProviders() {
        return ResponseEntity.ok(shippingService.getAllProviders());
    }

    @PostMapping("/calculate-fee")
    public ResponseEntity<BigDecimal> calculateFee(
            @RequestParam String providerId,
            @RequestBody Address address) {
        // For inquiry, we can pass 0 or a dummy value if we don't have items.
        // Ideally, frontend should send cart total.
        return ResponseEntity.ok(shippingService.calculateShippingFee(providerId, address, java.math.BigDecimal.ZERO));
    }
}
