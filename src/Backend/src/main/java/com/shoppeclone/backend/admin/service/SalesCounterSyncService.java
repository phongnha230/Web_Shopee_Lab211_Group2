package com.shoppeclone.backend.admin.service;

import com.shoppeclone.backend.order.entity.Order;
import com.shoppeclone.backend.order.entity.OrderItem;
import com.shoppeclone.backend.order.entity.OrderStatus;
import com.shoppeclone.backend.order.repository.OrderRepository;
import com.shoppeclone.backend.product.entity.Product;
import com.shoppeclone.backend.product.entity.ProductVariant;
import com.shoppeclone.backend.product.repository.ProductRepository;
import com.shoppeclone.backend.product.repository.ProductVariantRepository;
import com.shoppeclone.backend.promotion.flashsale.entity.FlashSale;
import com.shoppeclone.backend.promotion.flashsale.entity.FlashSaleItem;
import com.shoppeclone.backend.promotion.flashsale.repository.FlashSaleItemRepository;
import com.shoppeclone.backend.promotion.flashsale.repository.FlashSaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesCounterSyncService {

    private static final Set<String> LIVE_FLASH_SALE_STATUSES = Set.of("ONGOING", "ACTIVE");

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;

    @Transactional
    public Map<String, Object> syncSalesCounters() {
        LocalDateTime now = LocalDateTime.now();

        List<Product> products = productRepository.findAll();
        List<ProductVariant> variants = productVariantRepository.findAll();
        List<Order> completedOrders = orderRepository.findAll().stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.COMPLETED)
                .collect(Collectors.toList());

        Map<String, Integer> productSoldMap = new HashMap<>();
        for (Order order : completedOrders) {
            for (OrderItem item : order.getItems()) {
                productVariantRepository.findById(item.getVariantId()).ifPresent(variant ->
                        productSoldMap.merge(variant.getProductId(), item.getQuantity(), Integer::sum));
            }
        }

        Map<String, FlashSale> flashSaleById = flashSaleRepository.findAll().stream()
                .collect(Collectors.toMap(FlashSale::getId, flashSale -> flashSale, (left, right) -> left));

        Map<String, Integer> productFlashSaleSoldMap = new HashMap<>();
        Map<String, Integer> variantFlashSaleSoldMap = new HashMap<>();
        for (FlashSaleItem item : flashSaleItemRepository.findAll()) {
            if (!"APPROVED".equalsIgnoreCase(item.getStatus())) {
                continue;
            }

            FlashSale slot = flashSaleById.get(item.getFlashSaleId());
            if (slot == null) {
                continue;
            }

            String status = slot.getStatus() != null ? slot.getStatus().toUpperCase() : "";
            if (!LIVE_FLASH_SALE_STATUSES.contains(status)) {
                continue;
            }

            int saleStock = item.getSaleStock() != null ? item.getSaleStock() : 0;
            int remainingStock = item.getRemainingStock() != null ? item.getRemainingStock() : 0;
            int sold = Math.max(0, saleStock - remainingStock);
            if (sold == 0) {
                continue;
            }

            if (item.getProductId() != null && !item.getProductId().isBlank()) {
                productFlashSaleSoldMap.merge(item.getProductId(), sold, Integer::sum);
            }
            if (item.getVariantId() != null && !item.getVariantId().isBlank()) {
                variantFlashSaleSoldMap.merge(item.getVariantId(), sold, Integer::sum);
            }
        }

        int updatedProducts = 0;
        for (Product product : products) {
            int sold = productSoldMap.getOrDefault(product.getId(), 0);
            int flashSaleSold = productFlashSaleSoldMap.getOrDefault(product.getId(), 0);
            boolean changed = !Integer.valueOf(sold).equals(product.getSold())
                    || !Integer.valueOf(flashSaleSold).equals(product.getFlashSaleSold());
            product.setSold(sold);
            product.setFlashSaleSold(flashSaleSold);
            product.setUpdatedAt(now);
            productRepository.save(product);
            if (changed) {
                updatedProducts++;
            }
        }

        int updatedVariants = 0;
        for (ProductVariant variant : variants) {
            int flashSaleSold = variantFlashSaleSoldMap.getOrDefault(variant.getId(), 0);
            boolean changed = !Integer.valueOf(flashSaleSold).equals(variant.getFlashSaleSold());
            variant.setFlashSaleSold(flashSaleSold);
            variant.setUpdatedAt(now);
            productVariantRepository.save(variant);
            if (changed) {
                updatedVariants++;
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("completedOrders", completedOrders.size());
        result.put("productsScanned", products.size());
        result.put("variantsScanned", variants.size());
        result.put("productsUpdated", updatedProducts);
        result.put("variantsUpdated", updatedVariants);
        result.put("productSoldEntries", productSoldMap.size());
        result.put("productFlashSaleEntries", productFlashSaleSoldMap.size());
        result.put("variantFlashSaleEntries", variantFlashSaleSoldMap.size());
        result.put("syncedAt", now);
        return result;
    }
}
