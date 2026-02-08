package com.shoppeclone.backend.shipping.controller;

import com.shoppeclone.backend.shipping.entity.ShippingProvider;
import com.shoppeclone.backend.shipping.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shipping-providers")
@RequiredArgsConstructor
public class ShippingProviderController {

    private final ShippingService shippingService;

    @GetMapping
    public ResponseEntity<List<ShippingProvider>> getAllProviders() {
        return ResponseEntity.ok(shippingService.getAllProviders());
    }
}
