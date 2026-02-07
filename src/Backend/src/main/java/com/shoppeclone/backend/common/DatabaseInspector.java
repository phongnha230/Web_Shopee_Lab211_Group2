package com.shoppeclone.backend.common;

import com.shoppeclone.backend.product.entity.Product;
import com.shoppeclone.backend.product.repository.ProductRepository;
import com.shoppeclone.backend.shop.entity.Shop;
import com.shoppeclone.backend.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInspector implements CommandLineRunner {

    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("========== DATABASE INSPECTION START ==========\n");

        List<Shop> shops = shopRepository.findAll();
        List<Product> products = productRepository.findAll();

        sb.append("Total Shops Found: ").append(shops.size()).append("\n");
        sb.append("Total Products Found: ").append(products.size()).append("\n");

        for (Shop shop : shops) {
            long count = products.stream()
                    .filter(p -> p.getShopId() != null && p.getShopId().equals(shop.getId()))
                    .count();
            sb.append("Shop: '").append(shop.getName()).append("' (ID: ").append(shop.getId()).append(") has ")
                    .append(count).append(" products\n");
        }

        List<Product> orphaned = products.stream()
                .filter(p -> p.getShopId() == null || shops.stream().noneMatch(s -> s.getId().equals(p.getShopId())))
                .collect(Collectors.toList());

        // CORRECTION: Move products from Baribanai to "Shop Công Nghệ Số 1"
        // User confirmed these 12 products belong to "Shop Công Nghệ Số 1".
        // Since Baribanai had 0 products before, we move ALL of them.

        Shop sourceShop = shops.stream()
                .filter(s -> "Baribanai".equalsIgnoreCase(s.getName()))
                .findFirst()
                .orElse(null);

        Shop correctTargetShop = shops.stream()
                .filter(s -> "Shop Công Nghệ Số 1".equalsIgnoreCase(s.getName()))
                .findFirst()
                .orElse(null);

        if (sourceShop != null && correctTargetShop != null) {
            sb.append("\nCORRECTION: Moving products from '").append(sourceShop.getName()).append("' to '")
                    .append(correctTargetShop.getName()).append("'...\n");

            List<Product> wronglyAssigned = products.stream()
                    .filter(p -> p.getShopId() != null && p.getShopId().equals(sourceShop.getId()))
                    .collect(Collectors.toList());

            if (!wronglyAssigned.isEmpty()) {
                for (Product p : wronglyAssigned) {
                    p.setShopId(correctTargetShop.getId());
                    productRepository.save(p);
                }
                sb.append("Successfully moved ").append(wronglyAssigned.size()).append(" products to ")
                        .append(correctTargetShop.getName()).append(".\n");
            } else {
                sb.append("No products found in source shop to move.\n");
            }
        }

        sb.append("========== DATABASE INSPECTION END ==========\n");

        java.nio.file.Files.writeString(java.nio.file.Path.of("inspector_result.txt"), sb.toString());
        log.info("Inspector result written to inspector_result.txt");
    }
}
