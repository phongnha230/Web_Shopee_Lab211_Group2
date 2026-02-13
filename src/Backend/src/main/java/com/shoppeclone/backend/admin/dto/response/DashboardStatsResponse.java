package com.shoppeclone.backend.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsResponse {
    private long totalUsers;
    private long activeShops;
    private long pendingShops;
    private long rejectedShops;
    private long totalOrders;
    private double totalGMV;
    private long totalDisputes;
    private long activeFlashSales;
    private long pendingFlashRegistrations;
    private long approvedFlashSaleItems;
    private long upcomingFlashSales;
    private long urgentActionsCount;

    // Operations Summary
    private long pendingOrders;
    private long openDisputes;
    private long pendingRegs; // Existing field for shops, maybe repurposed? No, let's keep separate or check
                              // naming.

    // Trend Data
    private List<TrendData> userTrend;
    private List<TrendData> shopTrend;
    private List<TrendData> disputeTrend;
    private List<TrendData> flashSaleTrend;

    // Distribution Data (e.g., Users by Role or Shops by Category)
    private List<DistributionData> distribution;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TrendData {
        private String date; // Format: "YYYY-MM-DD" or "HH:mm"
        private long count;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DistributionData {
        private String label;
        private long value;
        private int percentage;
    }

    // Top Selling Products
    private List<TopProduct> topProducts;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TopProduct {
        private String name;
        private double price; // Changed to double for simplicity in display
        private int sold;
        private String status; // active, out_of_stock, etc.
    }
}
