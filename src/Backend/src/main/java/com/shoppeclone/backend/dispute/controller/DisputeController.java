package com.shoppeclone.backend.dispute.controller;

import com.shoppeclone.backend.auth.model.User;
import com.shoppeclone.backend.auth.repository.UserRepository;
import com.shoppeclone.backend.dispute.dto.request.CreateDisputeRequest;
import com.shoppeclone.backend.dispute.dto.request.UploadDisputeImageRequest;
import com.shoppeclone.backend.dispute.entity.Dispute;
import com.shoppeclone.backend.dispute.entity.DisputeImage;
import com.shoppeclone.backend.dispute.service.DisputeService;
import com.shoppeclone.backend.order.entity.Order;
import com.shoppeclone.backend.order.repository.OrderRepository;
import com.shoppeclone.backend.shop.entity.Shop;
import com.shoppeclone.backend.shop.service.ShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
@RestController
@RequestMapping("/api/disputes")
@RequiredArgsConstructor
public class DisputeController {

    private final DisputeService disputeService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ShopService shopService;

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

    private void assertCanAccessOrder(UserDetails userDetails, String orderId) {
        User user = getCurrentUser(userDetails);
        if (hasRole(user, "ROLE_ADMIN")) {
            return;
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (user.getId().equals(order.getUserId())) {
            return;
        }

        try {
            Shop shop = shopService.getMyShop(user.getEmail());
            if (shop != null && shop.getId() != null && shop.getId().equals(order.getShopId())) {
                return;
            }
        } catch (RuntimeException ignored) {
            // No seller shop for this user -> deny below.
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No permission to access this dispute");
    }

    private void assertCanAccessDispute(UserDetails userDetails, Dispute dispute) {
        assertCanAccessOrder(userDetails, dispute.getOrderId());
    }

    @PostMapping
    public ResponseEntity<Dispute> createDispute(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateDisputeRequest requestBody) {
        String buyerId = getCurrentUser(userDetails).getId();

        return ResponseEntity.ok(disputeService.createDispute(
                requestBody.getOrderId(),
                buyerId,
                requestBody.getReason(),
                requestBody.getDescription()));
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<DisputeImage> uploadImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id,
            @Valid @RequestBody UploadDisputeImageRequest body) {
        Dispute dispute = disputeService.getDispute(id);
        assertCanAccessDispute(userDetails, dispute);

        String uploadedBy = getCurrentUser(userDetails).getId();

        return ResponseEntity.ok(disputeService.addDisputeImage(id, body.getImageUrl(), uploadedBy));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Dispute> getDispute(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id) {
        Dispute dispute = disputeService.getDispute(id);
        assertCanAccessDispute(userDetails, dispute);
        return ResponseEntity.ok(dispute);
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<List<DisputeImage>> getDisputeImages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id) {
        Dispute dispute = disputeService.getDispute(id);
        assertCanAccessDispute(userDetails, dispute);
        return ResponseEntity.ok(disputeService.getDisputeImages(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Dispute> getDisputeByOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String orderId) {
        assertCanAccessOrder(userDetails, orderId);
        return disputeService.findOptionalByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
