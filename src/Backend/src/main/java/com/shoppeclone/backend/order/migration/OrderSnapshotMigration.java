package com.shoppeclone.backend.order.migration;

import com.shoppeclone.backend.order.entity.Order;
import com.shoppeclone.backend.order.entity.OrderItem;
import com.shoppeclone.backend.order.repository.OrderRepository;
import com.shoppeclone.backend.product.entity.Product;
import com.shoppeclone.backend.product.entity.ProductImage;
import com.shoppeclone.backend.product.entity.ProductVariant;
import com.shoppeclone.backend.product.repository.ProductImageRepository;
import com.shoppeclone.backend.product.repository.ProductRepository;
import com.shoppeclone.backend.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderSnapshotMigration implements ApplicationRunner {

    private final OrderRepository orderRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    @Override
    public void run(ApplicationArguments args) {
        log.info("=== [Migration] Bat dau quet don hang thieu snapshot... ===");

        List<Order> allOrders = orderRepository.findAll();

        // NOTE:
        // Do NOT clear existing snapshots just because variantId no longer exists.
        // Snapshot fields are meant to preserve historical display when product/variant is
        // deleted. Clearing them here can cause product names/images to be lost again.

        // === Buoc 1: Migration chinh ===
        int updatedOrders = 0;
        int updatedItems = 0;
        int skippedItems = 0;

        for (Order order : allOrders) {
            if (order.getItems() == null || order.getItems().isEmpty())
                continue;

            boolean orderModified = false;

            for (OrderItem item : order.getItems()) {
                if (item.getProductName() != null && !item.getProductName().trim().isEmpty()) {
                    continue; // Da co snapshot roi
                }

                String variantId = item.getVariantId();
                if (variantId == null)
                    continue;

                try {
                    // Cach 1: Tim truc tiep qua variantId
                    ProductVariant variant = variantRepository.findById(variantId).orElse(null);

                    if (variant != null) {
                        Product product = productRepository.findById(variant.getProductId()).orElse(null);
                        fillSnapshot(item, variant, product);
                        orderModified = true;
                        updatedItems++;
                        log.info("  [OK-1] Order {} -> '{}'", order.getId(), item.getProductName());
                        continue;
                    }

                    // Cach 2: Variant bi xoa -> tim qua shopId + gia GAN NHAT
                    String shopId = order.getShopId();
                    if (shopId == null) {
                        skippedItems++;
                        continue;
                    }

                    List<Product> shopProducts = productRepository.findByShopId(shopId);
                    Product bestProduct = null;
                    ProductVariant bestVariant = null;
                    BigDecimal bestDiff = null;

                    for (Product product : shopProducts) {
                        List<ProductVariant> pvs = variantRepository.findByProductId(product.getId());

                        for (ProductVariant pv : pvs) {
                            if (pv.getPrice() == null || item.getPrice() == null)
                                continue;

                            // Kiem tra gia thong thuong
                            BigDecimal diff = pv.getPrice().subtract(item.getPrice()).abs();
                            if (bestDiff == null || diff.compareTo(bestDiff) < 0) {
                                bestDiff = diff;
                                bestProduct = product;
                                bestVariant = pv;
                            }

                            // Kiem tra gia flash sale
                            if (pv.getFlashSalePrice() != null) {
                                BigDecimal fsDiff = pv.getFlashSalePrice().subtract(item.getPrice()).abs();
                                if (fsDiff.compareTo(bestDiff) < 0) {
                                    bestDiff = fsDiff;
                                    bestProduct = product;
                                    bestVariant = pv;
                                }
                            }
                        }
                    }

                    if (bestProduct != null) {
                        fillSnapshot(item, bestVariant, bestProduct);
                        orderModified = true;
                        updatedItems++;
                        log.info("  [OK-2] Order {} -> '{}' (gia order: {}, gia SP: {}, chenh: {})",
                                order.getId(), item.getProductName(), item.getPrice(),
                                bestVariant != null ? bestVariant.getPrice() : "?", bestDiff);
                    } else {
                        log.warn("  [SKIP] Order {} - khong tim duoc SP nao trong shop {}", order.getId(), shopId);
                        skippedItems++;
                    }

                } catch (Exception e) {
                    log.error("  [ERROR] variant {} trong order {}: {}", variantId, order.getId(), e.getMessage());
                    skippedItems++;
                }
            }

            if (orderModified) {
                orderRepository.save(order);
                updatedOrders++;
            }
        }

        log.info("=== [Migration] Hoan tat! Tong: {} don, cap nhat {} don / {} item, bo qua {} ===",
                allOrders.size(), updatedOrders, updatedItems, skippedItems);
    }

    private void fillSnapshot(OrderItem item, ProductVariant variant, Product product) {
        if (product != null) {
            item.setProductName(product.getName());
        }
        item.setVariantName(buildVariantName(variant));
        String imageUrl = resolveProductImage(variant, product);
        if (imageUrl != null) {
            item.setProductImage(imageUrl);
        }
    }

    private String buildVariantName(ProductVariant variant) {
        if (variant == null)
            return "Default";
        String name = ((variant.getColor() != null ? variant.getColor() : "")
                + (variant.getSize() != null && !variant.getSize().isEmpty()
                        ? " - " + variant.getSize()
                        : ""))
                .trim();
        return name.isEmpty() ? "Default" : name;
    }

    private String resolveProductImage(ProductVariant variant, Product product) {
        if (variant != null && variant.getImageUrl() != null && !variant.getImageUrl().isEmpty()) {
            return variant.getImageUrl();
        }
        if (product != null) {
            List<ProductImage> images = productImageRepository
                    .findByProductIdOrderByDisplayOrderAsc(product.getId());
            if (!images.isEmpty()) {
                return images.get(0).getImageUrl();
            }
        }
        return null;
    }
}
