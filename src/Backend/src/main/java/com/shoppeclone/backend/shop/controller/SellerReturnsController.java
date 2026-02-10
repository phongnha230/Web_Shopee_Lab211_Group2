package com.shoppeclone.backend.shop.controller;

import com.shoppeclone.backend.dispute.entity.Dispute;
import com.shoppeclone.backend.dispute.repository.DisputeRepository;
import com.shoppeclone.backend.order.entity.Order;
import com.shoppeclone.backend.order.repository.OrderRepository;
import com.shoppeclone.backend.refund.entity.Refund;
import com.shoppeclone.backend.refund.repository.RefundRepository;
import com.shoppeclone.backend.shop.entity.Shop;
import com.shoppeclone.backend.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
public class SellerReturnsController {

    private final ShopService shopService;
    private final OrderRepository orderRepository;
    private final DisputeRepository disputeRepository;
    private final RefundRepository refundRepository;

    /**
     * Lấy danh sách disputes và refunds cho các đơn hàng thuộc shop của seller.
     */
    @GetMapping("/returns")
    public ResponseEntity<Map<String, Object>> getShopReturns(
            @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        Shop shop = shopService.getMyShop(userEmail);

        List<Order> shopOrders = orderRepository.findByShopId(shop.getId());
        List<String> orderIds = shopOrders.stream().map(Order::getId).collect(Collectors.toList());

        List<Dispute> disputes = orderIds.isEmpty()
                ? List.of()
                : disputeRepository.findByOrderIdIn(orderIds);
        List<Refund> refunds = orderIds.isEmpty()
                ? List.of()
                : refundRepository.findByOrderIdIn(orderIds);

        Map<String, Object> result = new HashMap<>();
        result.put("disputes", disputes);
        result.put("refunds", refunds);
        return ResponseEntity.ok(result);
    }
}
