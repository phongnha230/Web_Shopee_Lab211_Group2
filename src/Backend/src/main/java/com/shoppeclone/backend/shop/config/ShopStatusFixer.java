package com.shoppeclone.backend.shop.config;

import com.shoppeclone.backend.shop.entity.Shop;
import com.shoppeclone.backend.shop.entity.ShopStatus;
import com.shoppeclone.backend.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@Order(2) // Run after DataInitializer (which is Order 1 by default)
@RequiredArgsConstructor
public class ShopStatusFixer implements CommandLineRunner {

    private final ShopRepository shopRepository;

    @Override
    public void run(String... args) {
        System.out.println("🔧 Running ShopStatusFixer...");

        // List of shop names that should be reverted to PENDING
        List<String> shopsToRevert = Arrays.asList("Arina", "Baribanai");

        for (String shopName : shopsToRevert) {
            shopRepository.findByName(shopName).ifPresent(shop -> {
                if (shop.getStatus() == ShopStatus.ACTIVE) {
                    shop.setStatus(ShopStatus.PENDING);
                    shop.setUpdatedAt(LocalDateTime.now());
                    shopRepository.save(shop);
                    System.out.println("✅ Reverted shop '" + shopName + "' to PENDING status");
                } else {
                    System.out.println("ℹ️ Shop '" + shopName + "' is already " + shop.getStatus());
                }
            });
        }

        System.out.println("✅ ShopStatusFixer completed!");
    }
}
