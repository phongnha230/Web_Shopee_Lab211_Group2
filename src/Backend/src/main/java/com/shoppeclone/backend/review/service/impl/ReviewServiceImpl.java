package com.shoppeclone.backend.review.service.impl;

import com.shoppeclone.backend.order.entity.Order;
import com.shoppeclone.backend.order.entity.OrderStatus;
import com.shoppeclone.backend.order.repository.OrderRepository;
import com.shoppeclone.backend.product.entity.ProductVariant;
import com.shoppeclone.backend.product.repository.ProductVariantRepository;
import com.shoppeclone.backend.review.dto.request.CreateReviewRequest;
import com.shoppeclone.backend.review.dto.request.UpdateReviewRequest;
import com.shoppeclone.backend.review.dto.response.ReviewResponse;
import com.shoppeclone.backend.review.entity.Review;
import com.shoppeclone.backend.review.exception.ReviewException;
import com.shoppeclone.backend.review.repository.ReviewRepository;
import com.shoppeclone.backend.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final ProductVariantRepository productVariantRepository;

    private static final int REVIEW_TIMEFRAME_DAYS = 30;

    @Override
    public ReviewResponse createReview(CreateReviewRequest request) {
        // 1. Check if order exists
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ReviewException("Order not found with id: " + request.getOrderId()));

        // 2. Check user ownership
        if (!order.getUserId().equals(request.getUserId())) {
            throw new ReviewException("You can only review your own orders");
        }

        // 3. Check order status - must be COMPLETED
        if (order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new ReviewException(
                    "You can only review orders that have been completed. Current status: " + order.getOrderStatus());
        }

        // 4. Check if product exists in the order (via variant)
        boolean productExistsInOrder = order.getItems().stream()
                .anyMatch(item -> {
                    ProductVariant variant = productVariantRepository.findById(item.getVariantId()).orElse(null);
                    return variant != null && variant.getProductId().equals(request.getProductId());
                });

        if (!productExistsInOrder) {
            throw new ReviewException("Product " + request.getProductId() + " was not found in this order");
        }

        // 5. Check for duplicate review
        boolean alreadyReviewed = reviewRepository.existsByUserIdAndProductIdAndOrderId(
                request.getUserId(),
                request.getProductId(),
                request.getOrderId());

        if (alreadyReviewed) {
            throw new ReviewException("You have already reviewed this product from this order");
        }

        // 6. Check timeframe - reviews allowed within 30 days of completion
        if (order.getCompletedAt() == null) {
            throw new ReviewException("Order completion date is not set");
        }

        LocalDateTime reviewDeadline = order.getCompletedAt().plusDays(REVIEW_TIMEFRAME_DAYS);
        if (LocalDateTime.now().isAfter(reviewDeadline)) {
            throw new ReviewException("Review period has expired. You can only review within " + REVIEW_TIMEFRAME_DAYS
                    + " days after order completion");
        }

        // All validations passed - create review
        Review review = new Review();
        review.setUserId(request.getUserId());
        review.setProductId(request.getProductId());
        review.setOrderId(request.getOrderId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        // Set image URLs if provided
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            review.setImageUrls(request.getImageUrls());
        }

        review.setCreatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);
        return mapToResponse(savedReview);
    }

    @Override
    public List<ReviewResponse> getReviewsByProductId(String productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        return reviews.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponse> getReviewsByUserId(String userId) {
        List<Review> reviews = reviewRepository.findByUserId(userId);
        return reviews.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ReviewResponse getReviewById(String id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
        return mapToResponse(review);
    }

    @Override
    public ReviewResponse updateReview(String id, UpdateReviewRequest request) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));

        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }
        if (request.getImageUrls() != null) {
            review.setImageUrls(request.getImageUrls());
        }

        Review updatedReview = reviewRepository.save(review);
        return mapToResponse(updatedReview);
    }

    @Override
    public void deleteReview(String id) {
        if (!reviewRepository.existsById(id)) {
            throw new RuntimeException("Review not found with id: " + id);
        }
        reviewRepository.deleteById(id);
    }

    @Override
    public Double getAverageRating(String productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);

        if (reviews.isEmpty()) {
            return 0.0;
        }

        double sum = reviews.stream()
                .mapToInt(Review::getRating)
                .sum();

        return sum / reviews.size();
    }

    @Override
    public boolean canUserReviewOrder(String userId, String orderId, String productId) {
        try {
            // Check if order exists
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                return false;
            }

            // Check user ownership
            if (!order.getUserId().equals(userId)) {
                return false;
            }

            // Check order status
            if (order.getOrderStatus() != OrderStatus.COMPLETED) {
                return false;
            }

            // Check if product exists in order (via variant)
            boolean productExistsInOrder = order.getItems().stream()
                    .anyMatch(item -> {
                        ProductVariant variant = productVariantRepository.findById(item.getVariantId()).orElse(null);
                        return variant != null && variant.getProductId().equals(productId);
                    });
            if (!productExistsInOrder) {
                return false;
            }

            // Check if already reviewed
            boolean alreadyReviewed = reviewRepository.existsByUserIdAndProductIdAndOrderId(
                    userId, productId, orderId);
            if (alreadyReviewed) {
                return false;
            }

            // Check timeframe
            if (order.getCompletedAt() == null) {
                return false;
            }

            LocalDateTime reviewDeadline = order.getCompletedAt().plusDays(REVIEW_TIMEFRAME_DAYS);
            if (LocalDateTime.now().isAfter(reviewDeadline)) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<String> getReviewableOrderIds(String userId) {
        // Find all completed orders for the user
        List<Order> completedOrders = orderRepository.findByUserIdAndOrderStatus(userId, OrderStatus.COMPLETED);

        LocalDateTime now = LocalDateTime.now();

        return completedOrders.stream()
                .filter(order -> {
                    // Filter by timeframe
                    if (order.getCompletedAt() == null) {
                        return false;
                    }
                    LocalDateTime deadline = order.getCompletedAt().plusDays(REVIEW_TIMEFRAME_DAYS);
                    if (now.isAfter(deadline)) {
                        return false;
                    }

                    // Check if any product in the order can be reviewed
                    return order.getItems().stream()
                            .anyMatch(item -> {
                                ProductVariant variant = productVariantRepository.findById(item.getVariantId())
                                        .orElse(null);
                                if (variant == null)
                                    return false;
                                return !reviewRepository.existsByUserIdAndProductIdAndOrderId(
                                        userId, variant.getProductId(), order.getId());
                            });

                })
                .map(Order::getId)
                .collect(Collectors.toList());
    }

    private ReviewResponse mapToResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setUserId(review.getUserId());
        response.setProductId(review.getProductId());
        response.setOrderId(review.getOrderId());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setImageUrls(review.getImageUrls()); // Include image URLs
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }
}
