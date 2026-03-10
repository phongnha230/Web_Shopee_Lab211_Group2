package com.shoppeclone.backend.cart.service.impl;

import com.shoppeclone.backend.cart.dto.CartResponse;
import com.shoppeclone.backend.cart.entity.Cart;
import com.shoppeclone.backend.cart.entity.CartItem;
import com.shoppeclone.backend.cart.repository.CartRepository;
import com.shoppeclone.backend.product.entity.Product;
import com.shoppeclone.backend.product.entity.ProductVariant;
import com.shoppeclone.backend.product.repository.ProductCategoryRepository;
import com.shoppeclone.backend.product.repository.ProductImageRepository;
import com.shoppeclone.backend.product.repository.ProductRepository;
import com.shoppeclone.backend.product.repository.ProductVariantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    void getCart_usesFlashSalePriceWhenVariantFlashSaleIsActive() {
        Cart cart = new Cart();
        cart.setId("cart-1");
        cart.setUserId("user-1");

        CartItem item = new CartItem();
        item.setVariantId("variant-1");
        item.setQuantity(10);
        cart.setItems(List.of(item));

        ProductVariant variant = new ProductVariant();
        variant.setId("variant-1");
        variant.setProductId("product-1");
        variant.setPrice(new BigDecimal("120000"));
        variant.setStock(99);
        variant.setIsFlashSale(true);
        variant.setFlashSalePrice(new BigDecimal("1200"));
        variant.setFlashSaleStock(200);
        variant.setFlashSaleSold(10);
        variant.setFlashSaleEndTime(LocalDateTime.now().plusHours(1));

        Product product = new Product();
        product.setId("product-1");
        product.setName("Classic Cross Sandals");
        product.setShopId("shop-1");

        when(cartRepository.findByUserId("user-1")).thenReturn(Optional.of(cart));
        when(productVariantRepository.findById("variant-1")).thenReturn(Optional.of(variant));
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));
        when(productImageRepository.findByProductIdOrderByDisplayOrderAsc("product-1")).thenReturn(List.of());
        when(productCategoryRepository.findByProductId("product-1")).thenReturn(List.of());

        CartResponse response = cartService.getCart("user-1");

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getPrice()).isEqualByComparingTo("1200");
        assertThat(response.getItems().get(0).getTotalPrice()).isEqualByComparingTo("12000");
        assertThat(response.getTotalPrice()).isEqualByComparingTo("12000");
    }
}
