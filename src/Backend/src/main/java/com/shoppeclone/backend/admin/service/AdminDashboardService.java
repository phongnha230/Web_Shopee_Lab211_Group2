package com.shoppeclone.backend.admin.service;

import com.shoppeclone.backend.admin.dto.response.DashboardStatsResponse;
import com.shoppeclone.backend.auth.model.User;
import com.shoppeclone.backend.auth.repository.UserRepository;
import com.shoppeclone.backend.shop.entity.Shop;
import com.shoppeclone.backend.shop.entity.ShopStatus;
import com.shoppeclone.backend.shop.repository.ShopRepository;
import com.shoppeclone.backend.product.repository.ProductRepository;
import com.shoppeclone.backend.product.entity.Product;
import com.shoppeclone.backend.order.entity.OrderStatus;
import com.shoppeclone.backend.dispute.entity.DisputeStatus;
import com.shoppeclone.backend.promotion.flashsale.entity.FlashSaleItem;
import com.shoppeclone.backend.promotion.flashsale.repository.FlashSaleCampaignRepository;
import com.shoppeclone.backend.promotion.flashsale.repository.FlashSaleItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final com.shoppeclone.backend.dispute.repository.DisputeRepository disputeRepository;
    private final ProductRepository productRepository;
    private final com.shoppeclone.backend.promotion.flashsale.repository.FlashSaleCampaignRepository flashSaleCampaignRepository;
    private final com.shoppeclone.backend.promotion.flashsale.repository.FlashSaleItemRepository flashSaleItemRepository;

    public DashboardStatsResponse getDashboardStats(int days) {
        System.out.println("Fetching dashboard stats for period: " + days + " days");
        DashboardStatsResponse stats = new DashboardStatsResponse();

        // Total Counts
        long totalUsers = userRepository.count();
        stats.setTotalUsers(totalUsers);
        stats.setActiveShops(shopRepository.countByStatus(ShopStatus.ACTIVE));
        stats.setPendingShops(shopRepository.countByStatus(ShopStatus.PENDING));
        stats.setRejectedShops(shopRepository.countByStatus(ShopStatus.REJECTED));
        stats.setRejectedShops(shopRepository.countByStatus(ShopStatus.REJECTED));
        stats.setTotalDisputes(disputeRepository.count());
        stats.setActiveFlashSales(flashSaleCampaignRepository.countByStatus("ONGOING"));
        stats.setUpcomingFlashSales(flashSaleCampaignRepository.countByStatus("REGISTRATION_OPEN"));
        stats.setPendingFlashRegistrations(flashSaleItemRepository.countByStatus("PENDING"));
        stats.setApprovedFlashSaleItems(flashSaleItemRepository.countByStatus("APPROVED"));

        // Calculate Urgent Actions (e.g., Campaigns with deadline < 24h + Pending
        // items)
        LocalDateTime target = LocalDateTime.now().plusDays(1);
        long nearingDeadlines = flashSaleCampaignRepository.findAll().stream()
                .filter(c -> "REGISTRATION_OPEN".equals(c.getStatus()) &&
                        c.getRegistrationDeadline() != null &&
                        c.getRegistrationDeadline().isBefore(target))
                .count();
        stats.setUrgentActionsCount(nearingDeadlines + stats.getPendingFlashRegistrations());

        // Operations Summary
        stats.setOpenDisputes(disputeRepository.countByStatus(DisputeStatus.OPEN));
        stats.setPendingRegs(flashSaleItemRepository.countByStatus("PENDING"));

        stats.setTotalOrders(0);
        stats.setTotalGMV(0.0);

        // Trend Data Calculation
        if (days == 1) {
            LocalDate today = LocalDate.now();
            stats.setUserTrend(getHourlyTrend(today, "user"));
            stats.setShopTrend(getHourlyTrend(today, "shop"));
            stats.setDisputeTrend(getHourlyTrend(today, "dispute"));
        } else if (days == 0) {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            stats.setUserTrend(getHourlyTrend(yesterday, "user"));
            stats.setShopTrend(getHourlyTrend(yesterday, "shop"));
            stats.setDisputeTrend(getHourlyTrend(yesterday, "dispute"));
        } else {
            LocalDate today = LocalDate.now();
            stats.setUserTrend(getUserRegistrationTrend(today, days));
            stats.setShopTrend(getShopRegistrationTrend(today, days));
            stats.setDisputeTrend(getDisputeTrend(today, days));
            stats.setFlashSaleTrend(getFlashSaleTrend(today, days));
        }

        // User Distribution Calculation (ROLE_USER, ROLE_SELLER, ROLE_ADMIN)
        stats.setDistribution(calculateUserDistribution(totalUsers));

        // Top Selling Products
        stats.setTopProducts(getTopSellingProducts());

        return stats;
    }

    private List<DashboardStatsResponse.TopProduct> getTopSellingProducts() {
        return productRepository.findTop5ByOrderBySoldDesc().stream()
                .map(p -> new DashboardStatsResponse.TopProduct(
                        p.getName(),
                        p.getMinPrice() != null ? p.getMinPrice().doubleValue() : 0.0,
                        p.getSold(),
                        p.getStatus().toString()))
                .collect(Collectors.toList());
    }

    private List<DashboardStatsResponse.DistributionData> calculateUserDistribution(long totalUsers) {
        List<DashboardStatsResponse.DistributionData> dist = new ArrayList<>();
        if (totalUsers == 0)
            return dist;

        List<User> allUsers = userRepository.findAll();

        long buyers = 0;
        long sellers = 0;
        long admins = 0;

        for (User user : allUsers) {
            boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
            boolean isSeller = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_SELLER"));
            boolean isUser = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_USER"));

            if (isAdmin)
                admins++;
            if (isSeller)
                sellers++;
            if (isUser)
                buyers++;
        }

        dist.add(new DashboardStatsResponse.DistributionData("Buyers", buyers, (int) (buyers * 100 / totalUsers)));
        dist.add(new DashboardStatsResponse.DistributionData("Sellers", sellers, (int) (sellers * 100 / totalUsers)));
        dist.add(new DashboardStatsResponse.DistributionData("Admins", admins, (int) (admins * 100 / totalUsers)));

        return dist;
    }

    private List<DashboardStatsResponse.TrendData> getHourlyTrend(LocalDate date, String type) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        List<LocalDateTime> timestamps;
        if (type.equals("user")) {
            timestamps = userRepository.findAll().stream()
                    .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(start)
                            && u.getCreatedAt().isBefore(end))
                    .map(User::getCreatedAt)
                    .collect(Collectors.toList());
        } else if (type.equals("shop")) {
            timestamps = shopRepository.findAll().stream()
                    .filter(s -> s.getCreatedAt() != null && s.getCreatedAt().isAfter(start)
                            && s.getCreatedAt().isBefore(end))
                    .map(Shop::getCreatedAt)
                    .collect(Collectors.toList());
        } else {
            timestamps = disputeRepository.findAll().stream()
                    .filter(d -> d.getCreatedAt() != null && d.getCreatedAt().isAfter(start)
                            && d.getCreatedAt().isBefore(end))
                    .map(com.shoppeclone.backend.dispute.entity.Dispute::getCreatedAt)
                    .collect(Collectors.toList());
        }

        return groupAndFormatTrendHourly(timestamps);
    }

    private List<DashboardStatsResponse.TrendData> getUserRegistrationTrend(LocalDate endDate, int days) {
        LocalDateTime start = endDate.minusDays(days - 1).atStartOfDay();
        List<User> users = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(start))
                .collect(Collectors.toList());

        return groupAndFormatTrendDaily(users.stream().map(User::getCreatedAt).collect(Collectors.toList()), endDate,
                days);
    }

    private List<DashboardStatsResponse.TrendData> getShopRegistrationTrend(LocalDate endDate, int days) {
        LocalDateTime start = endDate.minusDays(days - 1).atStartOfDay();
        List<Shop> shops = shopRepository.findAll().stream()
                .filter(s -> s.getCreatedAt() != null && s.getCreatedAt().isAfter(start))
                .collect(Collectors.toList());

        return groupAndFormatTrendDaily(shops.stream().map(Shop::getCreatedAt).collect(Collectors.toList()), endDate,
                days);
    }

    private List<DashboardStatsResponse.TrendData> getDisputeTrend(LocalDate endDate, int days) {
        LocalDateTime start = endDate.minusDays(days - 1).atStartOfDay();
        List<com.shoppeclone.backend.dispute.entity.Dispute> disputes = disputeRepository.findAll().stream()
                .filter(d -> d.getCreatedAt() != null && d.getCreatedAt().isAfter(start))
                .collect(Collectors.toList());

        return groupAndFormatTrendDaily(
                disputes.stream().map(com.shoppeclone.backend.dispute.entity.Dispute::getCreatedAt)
                        .collect(Collectors.toList()),
                endDate,
                days);
    }

    private List<DashboardStatsResponse.TrendData> getFlashSaleTrend(LocalDate endDate, int days) {
        LocalDateTime start = endDate.minusDays(days - 1).atStartOfDay();
        List<com.shoppeclone.backend.promotion.flashsale.entity.FlashSaleItem> regs = flashSaleItemRepository.findAll()
                .stream()
                .filter(r -> r.getCreatedAt() != null && r.getCreatedAt().isAfter(start))
                .collect(Collectors.toList());

        return groupAndFormatTrendDaily(
                regs.stream().map(com.shoppeclone.backend.promotion.flashsale.entity.FlashSaleItem::getCreatedAt)
                        .collect(Collectors.toList()),
                endDate,
                days);
    }

    private List<DashboardStatsResponse.TrendData> groupAndFormatTrendDaily(List<LocalDateTime> timestamps,
            LocalDate endDate, int days) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        Map<String, Long> counts = new TreeMap<>();

        for (int i = 0; i < days; i++) {
            counts.put(endDate.minusDays(i).format(formatter), 0L);
        }

        for (LocalDateTime ts : timestamps) {
            String dateKey = ts.format(formatter);
            if (counts.containsKey(dateKey)) {
                counts.put(dateKey, counts.get(dateKey) + 1);
            }
        }

        List<DashboardStatsResponse.TrendData> trend = new ArrayList<>();
        counts.forEach((date, count) -> trend.add(new DashboardStatsResponse.TrendData(date, count)));
        return trend;
    }

    private List<DashboardStatsResponse.TrendData> groupAndFormatTrendHourly(List<LocalDateTime> timestamps) {
        Map<Integer, Long> counts = new TreeMap<>();
        for (int i = 0; i < 24; i++) {
            counts.put(i, 0L);
        }

        for (LocalDateTime ts : timestamps) {
            int hour = ts.getHour();
            counts.put(hour, counts.get(hour) + 1);
        }

        List<DashboardStatsResponse.TrendData> trend = new ArrayList<>();
        counts.forEach((hour, count) -> {
            String label = String.format("%02d:00", hour);
            trend.add(new DashboardStatsResponse.TrendData(label, count));
        });
        return trend;
    }
}
