package com.shoppeclone.backend.order.controller;

import com.shoppeclone.backend.auth.model.User;
import com.shoppeclone.backend.auth.repository.UserRepository;
import com.shoppeclone.backend.order.dto.OrderRequest;
import com.shoppeclone.backend.order.dto.OrderResponse;
import com.shoppeclone.backend.order.entity.Order;
import com.shoppeclone.backend.order.entity.OrderStatus;
import com.shoppeclone.backend.order.service.OrderResponseService;
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
    private final OrderResponseService orderResponseService;
    private final ShopService shopService;

    private String getUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody OrderRequest request) {
        String userId = getUserId(userDetails);
        Order order = orderService.createOrder(userId, request);
        OrderResponse response = orderResponseService.enrichWithReviewInfo(order, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getUserOrders(@AuthenticationPrincipal UserDetails userDetails) {
        String userId = getUserId(userDetails);
        List<Order> orders = orderService.getUserOrders(userId);
        List<OrderResponse> responses = orderResponseService.enrichWithReviewInfo(orders, userId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String orderId) {
        String userId = getUserId(userDetails);
        Order order = orderService.getOrder(orderId);
        // TODO: Validate user owns order or is admin
        OrderResponse response = orderResponseService.enrichWithReviewInfo(order, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String orderId,
            @RequestParam OrderStatus status) {
        String userId = getUserId(userDetails);
        // TODO: Check if admin/seller
        Order order = orderService.updateOrderStatus(orderId, status);
        OrderResponse response = orderResponseService.enrichWithReviewInfo(order, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String orderId) {
        String userId = getUserId(userDetails);
        // TODO: Check if user owns order
        Order order = orderService.cancelOrder(orderId);
        OrderResponse response = orderResponseService.enrichWithReviewInfo(order, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}/shipping")
    public ResponseEntity<OrderResponse> updateShipment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String orderId,
            @RequestParam(required = false) String trackingCode,
            @RequestParam(required = false) String providerId) {
        String userId = getUserId(userDetails);
        // TODO: Check if admin/seller
        Order order = orderService.updateShipment(orderId, trackingCode, providerId);
        OrderResponse response = orderResponseService.enrichWithReviewInfo(order, userId);
        return ResponseEntity.ok(response);
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

    @PutMapping("/{orderId}/assign-shipper")
    public ResponseEntity<OrderResponse> assignShipper(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String orderId,
            @RequestParam String shipperId) {
        String userId = getUserId(userDetails);
        // TODO: Check if admin/seller
        Order order = orderService.assignShipper(orderId, shipperId);
        OrderResponse response = orderResponseService.enrichWithReviewInfo(order, userId);
        return ResponseEntity.ok(response);
    }
}
