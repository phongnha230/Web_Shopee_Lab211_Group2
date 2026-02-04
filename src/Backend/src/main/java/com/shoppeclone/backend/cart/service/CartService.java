package com.shoppeclone.backend.cart.service;

import com.shoppeclone.backend.cart.dto.CartResponse;

public interface CartService {
    CartResponse getCart(String userId);

    CartResponse addToCart(String userId, String variantId, int quantity);

    CartResponse updateCartItem(String userId, String variantId, int quantity);

    CartResponse removeCartItem(String userId, String variantId);

    void clearCart(String userId);
}
