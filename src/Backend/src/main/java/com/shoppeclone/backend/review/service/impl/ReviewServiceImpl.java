package com.shoppeclone.backend.review.service.impl;

import com.shoppeclone.backend.order.entity.Order;
import com.shoppeclone.backend.order.entity.OrderStatus;
import com.shoppeclone.backend.order.repository.OrderRepository;
import com.shoppeclone.backend.product.entity.Product;
import com.shoppeclone.backend.product.entity.ProductVariant;
import com.shoppeclone.backend.product.repository.ProductRepository;
import com.shoppeclone.backend.product.repository.ProductVariantRepository;
import com.shoppeclone.backend.review.dto.request.CreateReviewRequest;
import com.shoppeclone.backend.review.dto.request.UpdateReviewRequest;
import com.shoppeclone.backend.review.dto.response.ReviewResponse;
import com.shoppeclone.backend.review.entity.Review;
import com.shoppeclone.backend.review.exception.ReviewException;
import com.shoppeclone.backend.review.repository.ReviewRepository;
import com.shoppeclone.backend.review.service.ReviewService;
import com.shoppeclone.backend.auth.model.User;
import com.shoppeclone.backend.auth.repository.UserRepository;
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
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

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
        syncProductRatingAndCount(request.getProductId());
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
    public ReviewResponse updateReview(String id, String userId, UpdateReviewRequest request) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
        if (!review.getUserId().equals(userId)) {
            throw new ReviewException("Bạn không có quyền sửa đánh giá này");
        }

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
        syncProductRatingAndCount(review.getProductId());
        return mapToResponse(updatedReview);
    }

    @Override
    public void deleteReview(String id, String userId) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
        if (!review.getUserId().equals(userId)) {
            throw new ReviewException("Bạn không có quyền xóa đánh giá này");
        }
        String productId = review.getProductId();
        reviewRepository.deleteById(id);
        syncProductRatingAndCount(productId);
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

    private void syncProductRatingAndCount(String productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return;

        List<Review> reviews = reviewRepository.findByProductId(productId);
        if (reviews.isEmpty()) {
            product.setRating(0.0);
            product.setReviewCount(0);
        } else {
            double avg = reviews.stream().mapToInt(Review::getRating).sum() / (double) reviews.size();
            product.setRating(Math.round(avg * 10.0) / 10.0); // 1 decimal
            product.setReviewCount(reviews.size());
        }
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    private ReviewResponse mapToResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setUserId(review.getUserId());
        String userName = userRepository.findById(review.getUserId())
                .map(User::getFullName)
                .filter(n -> n != null && !n.isBlank())
                .orElse("Khách hàng");
        response.setUserName(userName);
        response.setProductId(review.getProductId());
        response.setOrderId(review.getOrderId());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setImageUrls(review.getImageUrls());
        response.setCreatedAt(review.getCreatedAt());
        response.setVerifiedPurchase(review.getOrderId() != null && !review.getOrderId().isBlank());
        return response;
    }
}
