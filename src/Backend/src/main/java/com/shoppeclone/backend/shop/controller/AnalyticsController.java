package com.shoppeclone.backend.shop.controller;

import com.shoppeclone.backend.order.entity.Order;
import com.shoppeclone.backend.order.entity.OrderStatus;
import com.shoppeclone.backend.order.service.OrderService;
import com.shoppeclone.backend.shop.entity.Shop;
import com.shoppeclone.backend.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/seller/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final OrderService orderService;
    private final ShopService shopService;

    @GetMapping("/dashboard-stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(Authentication authentication) {
        Shop shop = shopService.getMyShop(authentication.getName());
        if (shop == null)
            return ResponseEntity.notFound().build();

        List<Order> allOrders = orderService.getOrdersByShopId(shop.getId(), null);

        long pendingOrders = allOrders.stream().filter(o -> o.getOrderStatus() == OrderStatus.PENDING).count();
        long completedOrders = allOrders.stream().filter(o -> o.getOrderStatus() == OrderStatus.COMPLETED).count();

        BigDecimal totalRevenue = allOrders.stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.COMPLETED)
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Revenue by day for the last 7 days
        Map<String, BigDecimal> revenueByDay = new TreeMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            revenueByDay.put(today.minusDays(i).toString(), BigDecimal.ZERO);
        }

        for (Order order : allOrders) {
            if (order.getOrderStatus() == OrderStatus.COMPLETED && order.getCreatedAt() != null) {
                String dateKey = order.getCreatedAt().toLocalDate().toString();
                if (revenueByDay.containsKey(dateKey)) {
                    revenueByDay.put(dateKey, revenueByDay.get(dateKey).add(order.getTotalPrice()));
                }
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("pendingOrders", pendingOrders);
        stats.put("completedOrders", completedOrders);
        stats.put("totalRevenue", totalRevenue);
        stats.put("revenueByDay", revenueByDay);

        return ResponseEntity.ok(stats);
    }
}
