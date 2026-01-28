package com.shoppeclone.backend.cart.service;

import com.shoppeclone.backend.cart.entity.Cart;

public interface CartService {
    Cart getCart(String userId);

    Cart addToCart(String userId, String variantId, int quantity);

    Cart updateCartItem(String userId, String variantId, int quantity); // update quantity specific item

    Cart removeCartItem(String userId, String variantId);

    void clearCart(String userId);
}
