package com.shoppeclone.backend.cart.controller;

import com.shoppeclone.backend.auth.model.User;
import com.shoppeclone.backend.auth.repository.UserRepository;
import com.shoppeclone.backend.cart.dto.CartResponse;
import com.shoppeclone.backend.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    private String getUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        String userId = getUserId(userDetails);
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String variantId,
            @RequestParam int quantity) {
        String userId = getUserId(userDetails);
        return ResponseEntity.ok(cartService.addToCart(userId, variantId, quantity));
    }

    @PutMapping("/update")
    public ResponseEntity<CartResponse> updateCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String variantId,
            @RequestParam int quantity) {
        String userId = getUserId(userDetails);
        return ResponseEntity.ok(cartService.updateCartItem(userId, variantId, quantity));
    }

    @DeleteMapping("/remove")
    public ResponseEntity<CartResponse> removeCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String variantId) {
        String userId = getUserId(userDetails);
        return ResponseEntity.ok(cartService.removeCartItem(userId, variantId));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        String userId = getUserId(userDetails);
        cartService.clearCart(userId);
        return ResponseEntity.ok().build();
    }
}
