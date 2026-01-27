package com.shoppeclone.backend.cart.service.impl;

import com.shoppeclone.backend.cart.entity.Cart;
import com.shoppeclone.backend.cart.entity.CartItem;
import com.shoppeclone.backend.cart.repository.CartRepository;
import com.shoppeclone.backend.cart.service.CartService;
import com.shoppeclone.backend.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    public Cart getCart(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUserId(userId);
                    return cartRepository.save(cart);
                });
    }

    @Override
    @Transactional
    public Cart addToCart(String userId, String variantId, int quantity) {
        // Verify variant exists
        if (!productVariantRepository.existsById(variantId)) {
            throw new RuntimeException("Product variant not found: " + variantId);
        }

        Cart cart = getCart(userId);
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getVariantId().equals(variantId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setVariantId(variantId);
            newItem.setQuantity(quantity);
            cart.getItems().add(newItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart updateCartItem(String userId, String variantId, int quantity) {
        Cart cart = getCart(userId);

        if (quantity <= 0) {
            return removeCartItem(userId, variantId);
        }

        cart.getItems().stream()
                .filter(item -> item.getVariantId().equals(variantId))
                .findFirst()
                .ifPresent(item -> item.setQuantity(quantity));

        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart removeCartItem(String userId, String variantId) {
        Cart cart = getCart(userId);
        cart.getItems().removeIf(item -> item.getVariantId().equals(variantId));
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void clearCart(String userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getItems().clear();
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);
        });
    }
}
