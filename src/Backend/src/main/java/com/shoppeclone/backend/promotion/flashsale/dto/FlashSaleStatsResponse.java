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

    // === Tổng quan chiến dịch ===
    private String flashSaleId;
    private String campaignId;
    private String flashSaleStatus; // ONGOING, ENDED, UPCOMING
    private String startTime;
    private String endTime;

    // === Thống kê tổng ===
    private int totalItems; // Tổng sản phẩm trong flash sale
    private int totalSaleStock; // Tổng stock phân bổ cho flash sale
    private int totalRemainingStock; // Tổng stock còn lại
    private int totalSold; // Tổng đã bán = totalSaleStock - totalRemainingStock
    private double soldPercentage; // % đã bán

    // === Chi tiết từng sản phẩm ===
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
        private String status; // PENDING / APPROVED / REJECTED
    }
}
