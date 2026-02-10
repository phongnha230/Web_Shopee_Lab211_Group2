package com.shoppeclone.backend.cart.service.impl;

import com.shoppeclone.backend.cart.dto.CartItemResponse;
import com.shoppeclone.backend.cart.dto.CartResponse;
import com.shoppeclone.backend.cart.entity.Cart;
import com.shoppeclone.backend.cart.entity.CartItem;
import com.shoppeclone.backend.cart.repository.CartRepository;
import com.shoppeclone.backend.cart.service.CartService;
import com.shoppeclone.backend.product.entity.Product;
import com.shoppeclone.backend.product.entity.ProductCategory;
import com.shoppeclone.backend.product.entity.ProductImage;
import com.shoppeclone.backend.product.entity.ProductVariant;
import com.shoppeclone.backend.product.repository.ProductCategoryRepository;
import com.shoppeclone.backend.product.repository.ProductImageRepository;
import com.shoppeclone.backend.product.repository.ProductRepository;
import com.shoppeclone.backend.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductCategoryRepository productCategoryRepository;

    @Override
    public CartResponse getCart(String userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                });
        return mapToCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addToCart(String userId, String variantId, int quantity) {
        // Verify variant exists
        if (!productVariantRepository.existsById(variantId)) {
            throw new RuntimeException("Product variant not found: " + variantId);
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                });

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
        Cart savedCart = cartRepository.save(cart);
        return mapToCartResponse(savedCart);
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(String userId, String variantId, int quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (quantity <= 0) {
            return removeCartItem(userId, variantId);
        }

        cart.getItems().stream()
                .filter(item -> item.getVariantId().equals(variantId))
                .findFirst()
                .ifPresent(item -> item.setQuantity(quantity));

        cart.setUpdatedAt(LocalDateTime.now());
        Cart savedCart = cartRepository.save(cart);
        return mapToCartResponse(savedCart);
    }

    @Override
    @Transactional
    public CartResponse removeCartItem(String userId, String variantId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.getItems().removeIf(item -> item.getVariantId().equals(variantId));
        cart.setUpdatedAt(LocalDateTime.now());
        Cart savedCart = cartRepository.save(cart);
        return mapToCartResponse(savedCart);
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

    private CartItemResponse toCartItemResponse(CartItem item) {
        Optional<ProductVariant> variantOpt = productVariantRepository.findById(item.getVariantId());
        if (variantOpt.isEmpty()) return null;

        ProductVariant variant = variantOpt.get();
        Optional<Product> productOpt = productRepository.findById(variant.getProductId());
        if (productOpt.isEmpty()) return null;

        Product product = productOpt.get();
        List<ProductImage> images = productImageRepository.findByProductIdOrderByDisplayOrderAsc(product.getId());
        String imageUrl = images.isEmpty() ? "" : images.get(0).getImageUrl();

        String variantName = (variant.getColor() != null ? variant.getColor() : "")
                + (variant.getSize() != null ? " - " + variant.getSize() : "");
        variantName = variantName.trim();
        if (variantName.startsWith("- ")) variantName = variantName.substring(2);

        List<String> categoryIds = productCategoryRepository.findByProductId(product.getId()).stream()
                .map(ProductCategory::getCategoryId)
                .collect(Collectors.toList());

        return CartItemResponse.builder()
                .variantId(item.getVariantId())
                .productId(product.getId())
                .categoryIds(categoryIds)
                .productName(product.getName())
                .productImage(imageUrl)
                .variantName(variantName.isEmpty() ? "Default" : variantName)
                .price(variant.getPrice())
                .quantity(item.getQuantity())
                .stock(variant.getStock())
                .totalPrice(variant.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .build();
    }

    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(this::toCartItemResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        BigDecimal totalPrice = itemResponses.stream()
                .map(CartItemResponse::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .items(itemResponses)
                .totalPrice(totalPrice)
                .build();
    }
}
