package com.shoppeclone.backend.promotion.flashsale.service.impl;

import com.shoppeclone.backend.product.entity.Product;
import com.shoppeclone.backend.product.entity.ProductVariant;
import com.shoppeclone.backend.product.repository.ProductRepository;
import com.shoppeclone.backend.product.repository.ProductVariantRepository;
import com.shoppeclone.backend.promotion.flashsale.entity.FlashSale;
import com.shoppeclone.backend.promotion.flashsale.entity.FlashSaleCampaign;
import com.shoppeclone.backend.promotion.flashsale.entity.FlashSaleItem;
import com.shoppeclone.backend.promotion.flashsale.repository.FlashSaleCampaignRepository;
import com.shoppeclone.backend.promotion.flashsale.repository.FlashSaleItemRepository;
import com.shoppeclone.backend.promotion.flashsale.repository.FlashSaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FlashSaleScheduler {

    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleCampaignRepository flashSaleCampaignRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    @Scheduled(fixedRate = 60000) // Run every minute
    public void processFlashSaleTransitions() {
        LocalDateTime now = LocalDateTime.now();
        log.debug("FlashSaleScheduler: Running transitions at VN time: {}", now);

        // 1. Process Campaigns
        List<FlashSaleCampaign> campaigns = flashSaleCampaignRepository.findAll();
        for (FlashSaleCampaign campaign : campaigns) {
            String status = campaign.getStatus();
            LocalDateTime start = campaign.getStartDate();
            LocalDateTime end = campaign.getEndDate();

            if ("REGISTRATION_OPEN".equals(status) && start != null && now.isAfter(start)) {
                campaign.setStatus("ONGOING");
                flashSaleCampaignRepository.save(campaign);
                log.info("Campaign '{}' moved to ONGOING", campaign.getName());
            } else if (!"FINISHED".equals(status) && end != null && now.isAfter(end)) {
                // Keep campaign open while there are still valid slots to run/register.
                boolean hasUsableSlot = flashSaleRepository.findByCampaignId(campaign.getId()).stream()
                        .anyMatch(slot -> {
                            String slotStatus = slot.getStatus() != null ? slot.getStatus().toUpperCase() : "";
                            boolean slotOpen = "ACTIVE".equals(slotStatus) || "ONGOING".equals(slotStatus);
                            return slotOpen
                                    && slot.getEndTime() != null
                                    && slot.getEndTime().isAfter(now);
                        });

                if (!hasUsableSlot) {
                    campaign.setStatus("FINISHED");
                    flashSaleCampaignRepository.save(campaign);
                    log.info("Campaign '{}' moved to FINISHED", campaign.getName());
                }
            }
        }

        // 2. Activate Slots and ensure items are active
        List<FlashSale> activeOrOngoingSlots = flashSaleRepository.findAll().stream()
                .filter(s -> ("ACTIVE".equals(s.getStatus()) || "ONGOING".equals(s.getStatus()))
                        && s.getStartTime() != null
                        && s.getEndTime() != null
                        && !s.getStartTime().isAfter(now)
                        && s.getEndTime().isAfter(now))
                .toList();

        for (FlashSale slot : activeOrOngoingSlots) {
            boolean isJustStarting = "ACTIVE".equals(slot.getStatus());
            if (isJustStarting) {
                log.info("Activating Flash Sale Slot: {}", slot.getId());
                slot.setStatus("ONGOING");
                flashSaleRepository.save(slot);
            }

            List<FlashSaleItem> items = flashSaleItemRepository.findByFlashSaleIdAndStatus(slot.getId(), "APPROVED");

            // ── When slot JUST STARTS: reset all flash sale sold counters ──
            // This prevents stale data from a previous campaign showing "HET HANG"
            if (isJustStarting) {
                for (FlashSaleItem item : items) {
                    // Reset Product level
                    Product product = productRepository.findById(item.getProductId()).orElse(null);
                    if (product != null) {
                        product.setFlashSaleSold(0);
                        productRepository.save(product);

                        // Reset ALL variants of this product
                        List<ProductVariant> allVariants = productVariantRepository.findByProductId(product.getId());
                        for (ProductVariant v : allVariants) {
                            v.setFlashSaleSold(0);
                            productVariantRepository.save(v);
                        }
                    }

                    // Note: specific variant reset is already covered by the "Reset ALL variants"
                    // block above.

                    // Reset FlashSaleItem remainingStock to full saleStock for the new campaign
                    if (item.getRemainingStock() == null || item.getRemainingStock() <= 0) {
                        item.setRemainingStock(item.getSaleStock());
                        flashSaleItemRepository.save(item);
                        log.info("Reset remainingStock to {} for flash sale item: {}", item.getSaleStock(),
                                item.getId());
                    }
                }
            }

            // ── Sync flash sale data to Product/Variant ──
            for (FlashSaleItem item : items) {
                Product product = productRepository.findById(item.getProductId()).orElse(null);
                if (product != null) {
                    log.debug("Syncing flash sale status for product: {} in slot: {}", product.getName(), slot.getId());
                    product.setIsFlashSale(true);
                    product.setFlashSalePrice(item.getSalePrice());
                    product.setFlashSaleStock(item.getSaleStock());

                    // ALWAYS SYNC: sold = saleStock - remainingStock
                    int total = item.getSaleStock() != null ? item.getSaleStock() : 0;
                    int remaining = item.getRemainingStock() != null ? item.getRemainingStock() : 0;
                    product.setFlashSaleSold(Math.max(0, total - remaining));

                    product.setFlashSaleEndTime(slot.getEndTime());
                    productRepository.save(product);

                    // Ensure ALL variants are in sync with the FlashSaleItem
                    List<ProductVariant> allVariants = productVariantRepository.findByProductId(product.getId());
                    for (ProductVariant v : allVariants) {
                        v.setIsFlashSale(true);
                        v.setFlashSalePrice(item.getSalePrice());
                        v.setFlashSaleStock(item.getSaleStock());
                        v.setFlashSaleSold(product.getFlashSaleSold()); // Sync with product level
                        v.setFlashSaleEndTime(slot.getEndTime());
                        productVariantRepository.save(v);
                    }
                }

                // Update Specific Variant if provided (granular update)
                if (item.getVariantId() != null) {
                    ProductVariant variant = productVariantRepository.findById(item.getVariantId()).orElse(null);
                    if (variant != null) {
                        variant.setIsFlashSale(true);
                        variant.setFlashSalePrice(item.getSalePrice());
                        variant.setFlashSaleStock(item.getSaleStock());

                        int total = item.getSaleStock() != null ? item.getSaleStock() : 0;
                        int remaining = item.getRemainingStock() != null ? item.getRemainingStock() : 0;
                        variant.setFlashSaleSold(Math.max(0, total - remaining));

                        variant.setFlashSaleEndTime(slot.getEndTime());
                        productVariantRepository.save(variant);
                    }
                }
            }
        }

        // 3. Deactivate Slots
        List<FlashSale> endingSlots = flashSaleRepository.findAll().stream()
                .filter(s -> "ONGOING".equals(s.getStatus())
                        && s.getEndTime() != null
                        && !s.getEndTime().isAfter(now))
                .toList();

        for (FlashSale slot : endingSlots) {
            log.info("Deactivating Flash Sale Slot: {}", slot.getId());
            slot.setStatus("FINISHED");
            flashSaleRepository.save(slot);

            List<FlashSaleItem> items = flashSaleItemRepository.findByFlashSaleIdAndStatus(slot.getId(), "APPROVED");
            for (FlashSaleItem item : items) {
                // Revert Product status
                Product product = productRepository.findById(item.getProductId()).orElse(null);
                if (product != null) {
                    product.setIsFlashSale(false);
                    product.setFlashSaleEndTime(null);
                    product.setFlashSaleSold(0); // Reset
                    productRepository.save(product);

                    // If Product level sale, revert ALL variants
                    List<ProductVariant> allVariants = productVariantRepository.findByProductId(product.getId());
                    for (ProductVariant v : allVariants) {
                        v.setIsFlashSale(false);
                        v.setFlashSalePrice(null);
                        v.setFlashSaleStock(null);
                        v.setFlashSaleSold(0);
                        v.setFlashSaleEndTime(null);
                        // Convert remaining stock back to regular stock?
                        // Simpler to just reset sale flags. Stock management is complex here,
                        // but sticking to requirement: just reset flags.
                        productVariantRepository.save(v);
                    }
                }

                // Restore remaining stock to Variant AND Reset Flash Sale Status
                if (item.getVariantId() != null) {
                    ProductVariant variant = productVariantRepository.findById(item.getVariantId()).orElse(null);
                    if (variant != null) {
                        variant.setIsFlashSale(false);
                        variant.setFlashSalePrice(null);
                        variant.setFlashSaleStock(null);
                        variant.setFlashSaleSold(0);
                        variant.setFlashSaleEndTime(null);

                        if (item.getRemainingStock() != null && item.getRemainingStock() > 0) {
                            variant.setStock(variant.getStock() + item.getRemainingStock());
                        }
                        productVariantRepository.save(variant);
                    }
                }
            }
        }
    }
}
