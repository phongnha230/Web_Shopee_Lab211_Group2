package com.shoppeclone.backend.refund.controller;

import com.shoppeclone.backend.auth.model.User;
import com.shoppeclone.backend.auth.repository.UserRepository;
import com.shoppeclone.backend.order.entity.Order;
import com.shoppeclone.backend.order.entity.OrderStatus;
import com.shoppeclone.backend.order.repository.OrderRepository;
import com.shoppeclone.backend.refund.dto.request.RequestRefundRequest;
import com.shoppeclone.backend.refund.entity.Refund;
import com.shoppeclone.backend.refund.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    private User getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private boolean hasRole(User user, String roleName) {
        return user.getRoles() != null && user.getRoles().stream()
                .anyMatch(role -> roleName.equalsIgnoreCase(role.getName()));
    }

    /**
     * Nguoi mua yeu cau tra hang / hoan tien sau khi don COMPLETED.
     */
    @PostMapping("/{orderId}/request")
    public ResponseEntity<Refund> requestRefund(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String orderId,
            @Valid @RequestBody RequestRefundRequest requestBody) {

        User currentUser = getCurrentUser(userDetails);
        String buyerId = currentUser.getId();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chi co the yeu cau tra hang khi don da COMPLETED.");
        }

        if (!buyerId.equals(order.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ban khong co quyen yeu cau hoan tien cho don hang nay.");
        }

        BigDecimal amount;
        if (requestBody.getAmount() != null) {
            amount = requestBody.getAmount();
        } else {
            amount = order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO;
        }

        return ResponseEntity.ok(refundService.createRefund(orderId, buyerId, amount, requestBody.getReason()));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Refund> getRefundByOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String orderId) {
        User currentUser = getCurrentUser(userDetails);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        boolean isOwner = currentUser.getId().equals(order.getUserId());
        boolean isAdmin = hasRole(currentUser, "ROLE_ADMIN");
        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No permission to access this refund");
        }

        return refundService.findOptionalByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
