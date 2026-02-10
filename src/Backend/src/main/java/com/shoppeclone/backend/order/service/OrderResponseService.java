package com.shoppeclone.backend.order.service;

import com.shoppeclone.backend.order.dto.OrderItemDisplayDto;
import com.shoppeclone.backend.order.dto.OrderItemReviewInfo;
import com.shoppeclone.backend.order.dto.OrderResponse;
import com.shoppeclone.backend.order.entity.Order;
import com.shoppeclone.backend.order.entity.OrderStatus;
import com.shoppeclone.backend.payment.entity.Payment;
import com.shoppeclone.backend.payment.entity.PaymentMethod;
import com.shoppeclone.backend.payment.repository.PaymentMethodRepository;
import com.shoppeclone.backend.payment.repository.PaymentRepository;
import com.shoppeclone.backend.product.entity.Product;
import com.shoppeclone.backend.product.entity.ProductImage;
import com.shoppeclone.backend.product.entity.ProductVariant;
import com.shoppeclone.backend.product.repository.ProductImageRepository;
import com.shoppeclone.backend.product.repository.ProductRepository;
import com.shoppeclone.backend.product.repository.ProductVariantRepository;
import com.shoppeclone.backend.review.entity.Review;
import com.shoppeclone.backend.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderResponseService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final ReviewRepository reviewRepository;

    private static final int REVIEW_TIMEFRAME_DAYS = 30;

    /**
     * Enrich OrderResponse with review information, display items, and payment method
     */
    public OrderResponse enrichWithReviewInfo(Order order, String userId) {
        OrderResponse response = OrderResponse.fromOrder(order);

        // Enrich payment method
        List<Payment> payments = paymentRepository.findByOrderId(order.getId());
        if (!payments.isEmpty()) {
            paymentMethodRepository.findById(payments.get(0).getPaymentMethodId())
                    .ifPresent(pm -> response.setPaymentMethod(pm.getCode()));
        }
        if (response.getPaymentMethod() == null) {
            response.setPaymentMethod("COD");
        }

        // Enrich display items (productName, image, variantName)
        List<OrderItemDisplayDto> displayItems = new ArrayList<>();
        for (var item : order.getItems()) {
            ProductVariant variant = productVariantRepository.findById(item.getVariantId()).orElse(null);
            if (variant == null) continue;
            Product product = productRepository.findById(variant.getProductId()).orElse(null);
            if (product == null) continue;

            String imageUrl = variant.getImageUrl();
            if (imageUrl == null || imageUrl.isEmpty()) {
                List<ProductImage> images = productImageRepository.findByProductIdOrderByDisplayOrderAsc(product.getId());
                imageUrl = images.isEmpty() ? "https://via.placeholder.com/80" : images.get(0).getImageUrl();
            }
            String variantName = (variant.getColor() != null ? variant.getColor() : "")
                    + (variant.getSize() != null && !variant.getSize().isEmpty() ? " - " + variant.getSize() : "");
            variantName = variantName.trim();

            displayItems.add(OrderItemDisplayDto.builder()
                    .variantId(item.getVariantId())
                    .productId(product.getId())
                    .productName(product.getName())
                    .productImage(imageUrl)
                    .variantName(variantName.isEmpty() ? "Default" : variantName)
                    .quantity(item.getQuantity())
                    .price(item.getPrice())
                    .build());
        }
        response.setDisplayItems(displayItems);

        if (order.getOrderStatus() != OrderStatus.COMPLETED) {
            // Can't review non-completed orders
            response.setCanReviewAny(false);
            response.setItemReviewInfo(new ArrayList<>());
            return response;
        }

        // Check if order is within review timeframe
        boolean withinTimeframe = false;
        if (order.getCompletedAt() != null) {
            LocalDateTime deadline = order.getCompletedAt().plusDays(REVIEW_TIMEFRAME_DAYS);
            withinTimeframe = LocalDateTime.now().isBefore(deadline);
        }

        List<OrderItemReviewInfo> itemReviewInfoList = new ArrayList<>();
        boolean anyCanReview = false;

        for (var item : order.getItems()) {
            // Get product ID from variant
            ProductVariant variant = productVariantRepository.findById(item.getVariantId()).orElse(null);
            if (variant == null) {
                continue;
            }

            String productId = variant.getProductId();

            // Check if already reviewed
            List<Review> existingReviews = reviewRepository.findByUserIdAndOrderId(userId, order.getId());
            Review existingReview = existingReviews.stream()
                    .filter(r -> r.getProductId().equals(productId))
                    .findFirst()
                    .orElse(null);

            boolean hasReviewed = existingReview != null;
            boolean canReview = withinTimeframe && !hasReviewed && order.getUserId().equals(userId);

            if (canReview) {
                anyCanReview = true;
            }

            OrderItemReviewInfo info = OrderItemReviewInfo.builder()
                    .variantId(item.getVariantId())
                    .productId(productId)
                    .canReview(canReview)
                    .hasReviewed(hasReviewed)
                    .reviewId(hasReviewed ? existingReview.getId() : null)
                    .build();

            itemReviewInfoList.add(info);
        }

        response.setItemReviewInfo(itemReviewInfoList);
        response.setCanReviewAny(anyCanReview);

        return response;
    }

    /**
     * Enrich a list of orders with review information
     */
    public List<OrderResponse> enrichWithReviewInfo(List<Order> orders, String userId) {
        return orders.stream()
                .map(order -> enrichWithReviewInfo(order, userId))
                .toList();
    }

}
