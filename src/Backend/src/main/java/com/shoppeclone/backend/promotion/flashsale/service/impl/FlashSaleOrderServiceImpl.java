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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
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

    /**
     * ✅ ATOMIC Flash Sale Order — Không bao giờ âm kho
     *
     * Cơ chế:
     * 1. Tìm FlashSaleItem APPROVED cho variantId trong flash sale ONGOING
     * 2. Dùng MongoDB findAndModify: IF remainingStock >= quantity THEN decrement
     * ATOMICALLY
     * 3. Đồng thời trừ stock trong ProductVariant cũng atomic
     *
     * Tại sao không dùng @Transactional thông thường?
     * - MongoDB single-document operation là ATOMIC by nature
     * - findAndModify = 1 lệnh duy nhất, thread-safe, không cần lock
     * - Hiệu năng tốt hơn Pessimistic Lock khi flash sale lớn
     */
    @Override
    public FlashSaleOrderResult placeOrder(FlashSaleOrderRequest request) {
        long startTime = System.currentTimeMillis();
        String variantId = request.getVariantId();
        int quantity = request.getQuantity() != null ? request.getQuantity() : 1;

        log.debug("FlashSale order attempt: variantId={}, qty={}", variantId, quantity);

        try {
            // ── Step 1: Tìm active FlashSaleItem cho variant này ──────────────
            List<FlashSaleItem> candidates = flashSaleItemRepository
                    .findByVariantIdAndStatus(variantId, "APPROVED");

            // Lọc chỉ lấy item thuộc flash sale đang ONGOING
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
                return FlashSaleOrderResult.builder()
                        .success(false)
                        .status("ERROR")
                        .message("Không có chương trình Flash Sale đang diễn ra cho sản phẩm này")
                        .variantId(variantId)
                        .responseTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }

            // ── Step 2: Atomic decrement FlashSaleItem.remainingStock ─────────
            // Điều kiện: remainingStock >= quantity (không cho phép âm kho)
            // findAndModify trả về document SAU KHI update (hoặc null nếu không thỏa điều
            // kiện)
            Query flashItemQuery = new Query(
                    Criteria.where("_id").is(targetItem.getId())
                            .and("remainingStock").gte(quantity));
            Update flashItemUpdate = new Update().inc("remainingStock", -quantity);
            FindAndModifyOptions opts = FindAndModifyOptions.options().returnNew(true);

            FlashSaleItem updatedItem = mongoTemplate.findAndModify(
                    flashItemQuery, flashItemUpdate, opts, FlashSaleItem.class);

            if (updatedItem == null) {
                // Stock không đủ hoặc vừa hết — không được update
                log.info("OUT_OF_STOCK: flash sale item {} for variant {}", targetItem.getId(), variantId);
                return FlashSaleOrderResult.builder()
                        .success(false)
                        .status("OUT_OF_STOCK")
                        .message("Hết hàng Flash Sale! Bạn đến muộn rồi 😢")
                        .remainingStock(0)
                        .variantId(variantId)
                        .responseTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }

            // ── Step 3: Atomic decrement ProductVariant.stock ─────────────────
            // Cũng dùng findAndModify để đảm bảo không âm stock thật
            Query variantQuery = new Query(
                    Criteria.where("_id").is(variantId)
                            .and("stock").gte(quantity));
            Update variantUpdate = new Update().inc("stock", -quantity);
            ProductVariant updatedVariant = mongoTemplate.findAndModify(
                    variantQuery, variantUpdate, opts, ProductVariant.class);

            if (updatedVariant == null) {
                // Rollback flash sale item stock nếu variant bị lỗi
                log.warn("Variant stock insufficient after flash sale decrement — rolling back flash sale item");
                mongoTemplate.findAndModify(
                        new Query(Criteria.where("_id").is(targetItem.getId())),
                        new Update().inc("remainingStock", quantity),
                        FlashSaleItem.class);
                return FlashSaleOrderResult.builder()
                        .success(false)
                        .status("OUT_OF_STOCK")
                        .message("Hết hàng! (variant stock depleted)")
                        .remainingStock(0)
                        .variantId(variantId)
                        .responseTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }

            int remaining = updatedItem.getRemainingStock() != null ? updatedItem.getRemainingStock() : 0;
            log.info("✅ FlashSale SUCCESS: variant={}, remaining={}", variantId, remaining);

            return FlashSaleOrderResult.builder()
                    .success(true)
                    .status("SUCCESS")
                    .message("Đặt hàng thành công! Còn " + remaining + " sản phẩm")
                    .remainingStock(remaining)
                    .variantId(variantId)
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .build();

        } catch (Exception e) {
            log.error("Flash sale order error for variant {}: {}", variantId, e.getMessage(), e);
            return FlashSaleOrderResult.builder()
                    .success(false)
                    .status("ERROR")
                    .message("Lỗi server: " + e.getMessage())
                    .variantId(variantId)
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    /**
     * Lấy thống kê flash sale slot cho Admin xem
     */
    @Override
    public FlashSaleStatsResponse getStatsByFlashSaleId(String flashSaleId) {
        FlashSale slot = flashSaleRepository.findById(flashSaleId)
                .orElseThrow(() -> new RuntimeException("Flash sale not found: " + flashSaleId));

        List<FlashSaleItem> items = flashSaleItemRepository.findByFlashSaleId(flashSaleId);

        return buildStatsResponse(slot, items);
    }

    /**
     * Lấy stats flash sale của shop — Seller xem sản phẩm của mình
     */
    @Override
    public List<FlashSaleStatsResponse> getStatsByShopId(String shopId) {
        List<FlashSaleItem> shopItems = flashSaleItemRepository.findByShopId(shopId);
        if (shopItems.isEmpty())
            return new ArrayList<>();

        // Group by flashSaleId
        java.util.Map<String, List<FlashSaleItem>> grouped = new java.util.HashMap<>();
        for (FlashSaleItem item : shopItems) {
            grouped.computeIfAbsent(item.getFlashSaleId(), k -> new ArrayList<>()).add(item);
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

    // ── Private helpers ───────────────────────────────────────────────────────

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
                .items(itemStatsList)
                .build();
    }
}
