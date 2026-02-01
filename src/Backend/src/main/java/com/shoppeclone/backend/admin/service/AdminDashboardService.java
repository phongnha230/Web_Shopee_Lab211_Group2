package com.shoppeclone.backend.admin.service;

import com.shoppeclone.backend.admin.dto.response.DashboardStatsResponse;
import com.shoppeclone.backend.auth.repository.UserRepository;
import com.shoppeclone.backend.shop.entity.ShopStatus;
import com.shoppeclone.backend.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final com.shoppeclone.backend.dispute.repository.DisputeRepository disputeRepository;

    public DashboardStatsResponse getDashboardStats() {
        System.out.println("Fetching dashboard stats...");
        DashboardStatsResponse stats = new DashboardStatsResponse();

        // Count total users
        stats.setTotalUsers(userRepository.count());
        System.out.println("Total users: " + stats.getTotalUsers());

        // Count shops by status
        stats.setActiveShops(shopRepository.countByStatus(ShopStatus.ACTIVE));
        stats.setPendingShops(shopRepository.countByStatus(ShopStatus.PENDING));
        stats.setRejectedShops(shopRepository.countByStatus(ShopStatus.REJECTED));
        System.out.println("Active shops: " + stats.getActiveShops());

        // TODO: Add order statistics when Order entity is implemented
        stats.setTotalOrders(0);
        stats.setTotalGMV(0.0);
        stats.setTotalDisputes(disputeRepository.count());

        return stats;
    }
}
