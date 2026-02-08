package com.shoppeclone.backend.shipping.seeder;

import com.shoppeclone.backend.shipping.entity.ShippingProvider;
import com.shoppeclone.backend.shipping.repository.ShippingProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(4) // Run after VoucherSeeder
public class ShippingProviderSeeder implements CommandLineRunner {

    private final ShippingProviderRepository shippingProviderRepository;

    @Override
    public void run(String... args) throws Exception {
        // Always reset to ensure "standard" and "express" IDs exist
        shippingProviderRepository.deleteAll();
        System.out.println(">>> Cleared existing shipping providers.");

        System.out.println(">>> Seeding Shipping Providers...");

        // Standard Shipping
        ShippingProvider standard = new ShippingProvider();
        standard.setId("standard");
        standard.setName("Standard Shipping");
        standard.setApiEndpoint("https://api.standard-shipping.com");
        shippingProviderRepository.save(standard);

        // Express Shipping
        ShippingProvider express = new ShippingProvider();
        express.setId("express");
        express.setName("Express Shipping");
        express.setApiEndpoint("https://api.express-shipping.com");
        shippingProviderRepository.save(express);

        System.out.println(">>> Shipping Providers seeded successfully!");
    }
}
