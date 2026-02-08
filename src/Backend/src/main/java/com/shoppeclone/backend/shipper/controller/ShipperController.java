package com.shoppeclone.backend.shipper.controller;

import com.shoppeclone.backend.auth.model.User;
import com.shoppeclone.backend.auth.repository.UserRepository;
import com.shoppeclone.backend.order.dto.OrderResponse;
import com.shoppeclone.backend.order.entity.Order;
import com.shoppeclone.backend.order.service.OrderResponseService;
import com.shoppeclone.backend.shipper.dto.DeliveryUpdateRequest;
import com.shoppeclone.backend.shipper.service.ShipperService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipper")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SHIPPER')")
public class ShipperController {

    private final ShipperService shipperService;
    private final UserRepository userRepository;
    private final OrderResponseService orderResponseService;

    private String getUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getAssignedOrders(@AuthenticationPrincipal UserDetails userDetails) {
        String shipperId = getUserId(userDetails);
        List<Order> orders = shipperService.getAssignedOrders(shipperId);
        // Enrich with paymentMethod + collectCash for shipper display
        return ResponseEntity.ok(orderResponseService.enrichForShipper(orders));
    }

    @PutMapping("/orders/{orderId}/pickup")
    public ResponseEntity<Order> pickupOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String orderId) {
        String shipperId = getUserId(userDetails);
        return ResponseEntity.ok(shipperService.pickupOrder(shipperId, orderId));
    }

    @PutMapping("/orders/{orderId}/shipping")
    public ResponseEntity<Order> markAsShipping(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String orderId) {
        String shipperId = getUserId(userDetails);
        return ResponseEntity.ok(shipperService.markAsShipping(shipperId, orderId));
    }

    @PutMapping("/orders/{orderId}/complete")
    public ResponseEntity<Order> completeDelivery(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String orderId,
            @RequestBody(required = false) DeliveryUpdateRequest request) {
        String shipperId = getUserId(userDetails);
        return ResponseEntity.ok(shipperService.completeDelivery(shipperId, orderId, request));
    }

    @PutMapping("/orders/{orderId}/fail")
    public ResponseEntity<Order> failDelivery(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String orderId,
            @RequestBody DeliveryUpdateRequest request) {
        String shipperId = getUserId(userDetails);
        return ResponseEntity.ok(shipperService.failDelivery(shipperId, orderId, request));
    }
}
