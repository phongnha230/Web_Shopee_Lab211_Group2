package com.shoppeclone.backend.promotion.flashsale.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashSaleStatsResponse {

    private String flashSaleId;
    private String campaignId;
    private String flashSaleStatus;
    private String startTime;
    private String endTime;

    private int totalItems;
    private int totalSaleStock;
    private int totalRemainingStock;
    private int totalSold;
    private double soldPercentage;
    private long totalRequests;
    private long successCount;
    private long outOfStockCount;
    private long errorCount;
    private long avgResponseMs;
    private long minResponseMs;
    private long maxResponseMs;
    private boolean integrityCheckPassed;
    private String monitorStatus;
    private String monitorStartedAt;
    private String lastRequestAt;

    private List<ItemStats> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemStats {
        private String itemId;
        private String productId;
        private String variantId;
        private String shopId;
        private BigDecimal salePrice;
        private int saleStock;
        private int remainingStock;
        private int sold;
        private double soldPercentage;
        private String status;
    }
}
