package com.shoppeclone.backend.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
