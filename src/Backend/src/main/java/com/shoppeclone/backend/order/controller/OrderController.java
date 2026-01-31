package com.shoppeclone.backend.order.controller;

import com.shoppeclone.backend.auth.model.User;
import com.shoppeclone.backend.auth.repository.UserRepository;
import com.shoppeclone.backend.order.dto.OrderRequest;
import com.shoppeclone.backend.order.entity.Order;
import com.shoppeclone.backend.order.entity.OrderStatus;
import com.shoppeclone.backend.order.service.OrderService;
import com.shoppeclone.backend.shop.entity.Shop;
import com.shoppeclone.backend.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;
    private final ShopService shopService;

    private String getUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody OrderRequest request) {
        String userId = getUserId(userDetails);
        return ResponseEntity.ok(orderService.createOrder(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<Order>> getUserOrders(@AuthenticationPrincipal UserDetails userDetails) {
        String userId = getUserId(userDetails);
        return ResponseEntity.ok(orderService.getUserOrders(userId));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable String orderId) {
        // TODO: Validate user owns order or is admin
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam OrderStatus status) {
        // TODO: Check if admin/seller
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable String orderId) {
        // TODO: Check if user owns order
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }

    @PutMapping("/{orderId}/shipping")
    public ResponseEntity<Order> updateShipment(
            @PathVariable String orderId,
            @RequestParam(required = false) String trackingCode,
            @RequestParam(required = false) String providerId) {
        // TODO: Check if admin/seller
        return ResponseEntity.ok(orderService.updateShipment(orderId, trackingCode, providerId));
    }

    // Seller endpoint: Get orders for their shop
    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<Order>> getShopOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String shopId,
            @RequestParam(required = false) OrderStatus status) {
        // Verify that the user owns this shop
        String userEmail = userDetails.getUsername();
        Shop shop = shopService.getMyShop(userEmail);

        if (!shop.getId().equals(shopId)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        List<Order> orders = orderService.getOrdersByShopId(shopId, status);
        return ResponseEntity.ok(orders);
    }
}
