package com.shoppeclone.backend.review.service;

import com.shoppeclone.backend.review.dto.request.CreateReviewRequest;
import com.shoppeclone.backend.review.dto.request.UpdateReviewRequest;
import com.shoppeclone.backend.review.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewService {

    /**
     * Create a new review for a product
     */
    ReviewResponse createReview(CreateReviewRequest request);

    /**
     * Get all reviews for a specific product
     */
    List<ReviewResponse> getReviewsByProductId(String productId);

    /**
     * Get all reviews by a specific user
     */
    List<ReviewResponse> getReviewsByUserId(String userId);

    /**
     * Get a single review by ID
     */
    ReviewResponse getReviewById(String id);

    /**
     * Update an existing review (chỉ chủ review mới sửa được)
     */
    ReviewResponse updateReview(String id, String userId, UpdateReviewRequest request);

    /**
     * Delete a review (chỉ chủ review mới xóa được)
     */
    void deleteReview(String id, String userId);

    /**
     * Get average rating for a product
     */
    Double getAverageRating(String productId);

    /**
     * Check if a user can review a specific product from an order
     */
    boolean canUserReviewOrder(String userId, String orderId, String productId);

    /**
     * Get all orders that the user can review
     */
    List<String> getReviewableOrderIds(String userId);
}
