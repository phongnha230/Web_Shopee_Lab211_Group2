package com.shoppeclone.backend.promotion.flashsale.service.impl;

import com.shoppeclone.backend.product.entity.ProductVariant;
import com.shoppeclone.backend.promotion.flashsale.dto.FlashSaleOrderRequest;
import com.shoppeclone.backend.promotion.flashsale.dto.FlashSaleOrderResult;
import com.shoppeclone.backend.promotion.flashsale.dto.FlashSaleStatsResponse;
import com.shoppeclone.backend.promotion.flashsale.entity.FlashSale;
import com.shoppeclone.backend.promotion.flashsale.entity.FlashSaleItem;
import com.shoppeclone.backend.promotion.flashsale.repository.FlashSaleItemRepository;
import com.shoppeclone.backend.promotion.flashsale.repository.FlashSaleRepository;
import com.shoppeclone.backend.promotion.flashsale.service.FlashSaleOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlashSaleOrderServiceImpl implements FlashSaleOrderService {

    private final MongoTemplate mongoTemplate;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleRuntimeMetricsTracker runtimeMetricsTracker;

    @Override
    public FlashSaleOrderResult placeOrder(FlashSaleOrderRequest request) {
        long startTime = System.currentTimeMillis();
        String variantId = request.getVariantId();
        int quantity = request.getQuantity() != null ? request.getQuantity() : 1;

        log.debug("FlashSale order attempt: variantId={}, qty={}", variantId, quantity);

        String monitoredFlashSaleId = null;
        try {
            List<FlashSaleItem> candidates = flashSaleItemRepository
                    .findByVariantIdAndStatus(variantId, "APPROVED");

            FlashSaleItem targetItem = null;
            for (FlashSaleItem item : candidates) {
                FlashSale slot = flashSaleRepository.findById(item.getFlashSaleId()).orElse(null);
                if (slot != null && "ONGOING".equals(slot.getStatus())) {
                    targetItem = item;
                    break;
                }
            }

            if (targetItem == null) {
                log.warn("No active flash sale for variant: {}", variantId);
                return buildOrderResult(null, false, "ERROR",
                        "Khong co chuong trinh Flash Sale dang dien ra cho san pham nay",
                        0, variantId, startTime);
            }

            monitoredFlashSaleId = targetItem.getFlashSaleId();

            Query flashItemQuery = new Query(
                    Criteria.where("_id").is(targetItem.getId())
                            .and("remainingStock").gte(quantity));
            Update flashItemUpdate = new Update().inc("remainingStock", -quantity);
            FindAndModifyOptions opts = FindAndModifyOptions.options().returnNew(true);

            FlashSaleItem updatedItem = mongoTemplate.findAndModify(
                    flashItemQuery, flashItemUpdate, opts, FlashSaleItem.class);

            if (updatedItem == null) {
                log.info("OUT_OF_STOCK: flash sale item {} for variant {}", targetItem.getId(), variantId);
                return buildOrderResult(monitoredFlashSaleId, false, "OUT_OF_STOCK",
                        "Het hang Flash Sale! Ban den muon roi",
                        0, variantId, startTime);
            }

            Query variantQuery = new Query(
                    Criteria.where("_id").is(variantId)
                            .and("stock").gte(quantity));
            Update variantUpdate = new Update().inc("stock", -quantity);
            ProductVariant updatedVariant = mongoTemplate.findAndModify(
                    variantQuery, variantUpdate, opts, ProductVariant.class);

            if (updatedVariant == null) {
                log.warn("Variant stock insufficient after flash sale decrement - rolling back flash sale item");
                mongoTemplate.findAndModify(
                        new Query(Criteria.where("_id").is(targetItem.getId())),
                        new Update().inc("remainingStock", quantity),
                        FlashSaleItem.class);
                return buildOrderResult(monitoredFlashSaleId, false, "OUT_OF_STOCK",
                        "Het hang! (variant stock depleted)",
                        0, variantId, startTime);
            }

            int remaining = updatedItem.getRemainingStock() != null ? updatedItem.getRemainingStock() : 0;
            log.info("FlashSale SUCCESS: variant={}, remaining={}", variantId, remaining);

            return buildOrderResult(monitoredFlashSaleId, true, "SUCCESS",
                    "Dat hang thanh cong! Con " + remaining + " san pham",
                    remaining, variantId, startTime);

        } catch (Exception e) {
            log.error("Flash sale order error for variant {}: {}", variantId, e.getMessage(), e);
            return buildOrderResult(monitoredFlashSaleId, false, "ERROR",
                    "Loi server: " + e.getMessage(),
                    null, variantId, startTime);
        }
    }

    @Override
    public FlashSaleStatsResponse getStatsByFlashSaleId(String flashSaleId) {
        FlashSale slot = flashSaleRepository.findById(flashSaleId)
                .orElseThrow(() -> new RuntimeException("Flash sale not found: " + flashSaleId));

        List<FlashSaleItem> items = flashSaleItemRepository.findByFlashSaleIdAndStatus(flashSaleId, "APPROVED");
        return buildStatsResponse(slot, items);
    }

    @Override
    public FlashSaleStatsResponse getStatsByCampaignId(String campaignId) {
        FlashSale slot = selectPreferredSlot(flashSaleRepository.findByCampaignId(campaignId));
        if (slot == null) {
            throw new RuntimeException("Flash sale slot not found for campaign: " + campaignId);
        }
        List<FlashSaleItem> items = flashSaleItemRepository.findByFlashSaleIdAndStatus(slot.getId(), "APPROVED");
        return buildStatsResponse(slot, items);
    }

    @Override
    public List<FlashSaleStatsResponse> getStatsByShopId(String shopId) {
        List<FlashSaleItem> shopItems = flashSaleItemRepository.findByShopId(shopId);
        if (shopItems.isEmpty()) {
            return new ArrayList<>();
        }

        java.util.Map<String, List<FlashSaleItem>> grouped = new java.util.HashMap<>();
        for (FlashSaleItem item : shopItems) {
            if (!"APPROVED".equalsIgnoreCase(item.getStatus())) {
                continue;
            }
            grouped.computeIfAbsent(item.getFlashSaleId(), ignored -> new ArrayList<>()).add(item);
        }

        List<SlotGroup> slotGroups = new ArrayList<>();
        for (java.util.Map.Entry<String, List<FlashSaleItem>> entry : grouped.entrySet()) {
            FlashSale slot = flashSaleRepository.findById(entry.getKey()).orElse(null);
            if (slot != null) {
                slotGroups.add(new SlotGroup(slot, entry.getValue()));
            }
        }

        slotGroups.sort(Comparator
                .comparingInt((SlotGroup g) -> statusPriority(g.slot.getStatus()))
                .thenComparing(g -> g.slot.getStartTime(), Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(g -> g.slot.getEndTime(), Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(g -> g.slot.getId(), Comparator.nullsLast(Comparator.reverseOrder())));

        List<FlashSaleStatsResponse> results = new ArrayList<>();
        for (SlotGroup group : slotGroups) {
            results.add(buildStatsResponse(group.slot, group.items));
        }
        return results;
    }

    private FlashSaleOrderResult buildOrderResult(String flashSaleId, boolean success, String status,
            String message, Integer remainingStock, String variantId, long startTime) {
        long responseMs = System.currentTimeMillis() - startTime;
        runtimeMetricsTracker.record(flashSaleId, status, responseMs);
        return FlashSaleOrderResult.builder()
                .success(success)
                .status(status)
                .message(message)
                .remainingStock(remainingStock)
                .variantId(variantId)
                .responseTimeMs(responseMs)
                .build();
    }

    private FlashSaleStatsResponse buildStatsResponse(FlashSale slot, List<FlashSaleItem> items) {
        int totalSaleStock = 0;
        int totalRemaining = 0;
        List<FlashSaleStatsResponse.ItemStats> itemStatsList = new ArrayList<>();

        for (FlashSaleItem item : items) {
            int saleStock = item.getSaleStock() != null ? item.getSaleStock() : 0;
            int remaining = item.getRemainingStock() != null ? item.getRemainingStock() : 0;
            int sold = saleStock - remaining;

            totalSaleStock += saleStock;
            totalRemaining += remaining;

            double pct = saleStock > 0 ? (double) sold / saleStock * 100 : 0;

            itemStatsList.add(FlashSaleStatsResponse.ItemStats.builder()
                    .itemId(item.getId())
                    .productId(item.getProductId())
                    .variantId(item.getVariantId())
                    .shopId(item.getShopId())
                    .salePrice(item.getSalePrice())
                    .saleStock(saleStock)
                    .remainingStock(remaining)
                    .sold(sold)
                    .soldPercentage(Math.round(pct * 10.0) / 10.0)
                    .status(item.getStatus())
                    .build());
        }

        int totalSold = totalSaleStock - totalRemaining;
        double overallPct = totalSaleStock > 0 ? (double) totalSold / totalSaleStock * 100 : 0;
        boolean integrityCheckPassed = totalSold + totalRemaining == totalSaleStock;
        FlashSaleRuntimeMetricsTracker.Snapshot snapshot = runtimeMetricsTracker.snapshot(slot.getId());

        return FlashSaleStatsResponse.builder()
                .flashSaleId(slot.getId())
                .campaignId(slot.getCampaignId())
                .flashSaleStatus(slot.getStatus())
                .startTime(slot.getStartTime() != null ? slot.getStartTime().toString() : null)
                .endTime(slot.getEndTime() != null ? slot.getEndTime().toString() : null)
                .totalItems(items.size())
                .totalSaleStock(totalSaleStock)
                .totalRemainingStock(totalRemaining)
                .totalSold(totalSold)
                .soldPercentage(Math.round(overallPct * 10.0) / 10.0)
                .totalRequests(snapshot.getTotalRequests())
                .successCount(snapshot.getSuccessCount())
                .outOfStockCount(snapshot.getOutOfStockCount())
                .errorCount(snapshot.getErrorCount())
                .avgResponseMs(snapshot.getAvgResponseMs())
                .minResponseMs(snapshot.getMinResponseMs())
                .maxResponseMs(snapshot.getMaxResponseMs())
                .integrityCheckPassed(integrityCheckPassed)
                .monitorStatus(snapshot.getMonitorStatus())
                .monitorStartedAt(snapshot.getMonitorStartedAt())
                .lastRequestAt(snapshot.getLastRequestAt())
                .items(itemStatsList)
                .build();
    }

    private FlashSale selectPreferredSlot(List<FlashSale> slots) {
        return slots.stream()
                .sorted(Comparator
                        .comparingInt((FlashSale slot) -> statusPriority(slot.getStatus()))
                        .thenComparing(FlashSale::getStartTime, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(FlashSale::getEndTime, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(FlashSale::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .findFirst()
                .orElse(null);
    }

    private int statusPriority(String status) {
        if ("ONGOING".equalsIgnoreCase(status)) {
            return 0;
        }
        if ("ACTIVE".equalsIgnoreCase(status)) {
            return 1;
        }
        return 9;
    }

    private static class SlotGroup {
        private final FlashSale slot;
        private final List<FlashSaleItem> items;

        private SlotGroup(FlashSale slot, List<FlashSaleItem> items) {
            this.slot = slot;
            this.items = items;
        }
    }
}
