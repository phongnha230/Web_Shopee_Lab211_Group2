package com.shoppeclone.backend.order.service;

import com.shoppeclone.backend.order.dto.OrderItemReviewInfo;
import com.shoppeclone.backend.order.dto.OrderResponse;
import com.shoppeclone.backend.order.entity.Order;
import com.shoppeclone.backend.order.entity.OrderStatus;
import com.shoppeclone.backend.product.entity.ProductVariant;
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
    private final ReviewRepository reviewRepository;

    private static final int REVIEW_TIMEFRAME_DAYS = 30;

    /**
     * Enrich OrderResponse with review information
     */
    public OrderResponse enrichWithReviewInfo(Order order, String userId) {
        OrderResponse response = OrderResponse.fromOrder(order);

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
