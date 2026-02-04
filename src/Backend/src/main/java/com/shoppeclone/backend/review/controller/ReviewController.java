package com.shoppeclone.backend.review.controller;

import com.shoppeclone.backend.auth.model.User;
import com.shoppeclone.backend.auth.repository.UserRepository;
import com.shoppeclone.backend.common.service.CloudinaryService;
import com.shoppeclone.backend.review.dto.request.CreateReviewRequest;
import com.shoppeclone.backend.review.dto.request.UpdateReviewRequest;
import com.shoppeclone.backend.review.dto.response.ReviewResponse;
import com.shoppeclone.backend.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    private String getUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    /**
     * Create a new review
     * POST /api/reviews
     */
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateReviewRequest request) {
        String userId = getUserId(userDetails);
        request.setUserId(userId); // Override userId with authenticated user
        ReviewResponse response = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Upload review image
     * POST /api/reviews/upload-image
     */
    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, String>> uploadReviewImage(
            @RequestParam("file") MultipartFile file) throws IOException {
        String imageUrl = cloudinaryService.uploadImage(file, "reviews");
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }

    /**
     * Get all reviews for a product
     * GET /api/reviews/product/{productId}
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByProduct(@PathVariable String productId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByProductId(productId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Get all reviews by a user
     * GET /api/reviews/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByUser(@PathVariable String userId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByUserId(userId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Get a single review by ID
     * GET /api/reviews/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable String id) {
        ReviewResponse review = reviewService.getReviewById(id);
        return ResponseEntity.ok(review);
    }

    /**
     * Update a review
     * PUT /api/reviews/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable String id,
            @Valid @RequestBody UpdateReviewRequest request) {
        ReviewResponse response = reviewService.updateReview(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a review
     * DELETE /api/reviews/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable String id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get average rating for a product
     * GET /api/reviews/product/{productId}/average-rating
     */
    @GetMapping("/product/{productId}/average-rating")
    public ResponseEntity<Map<String, Double>> getAverageRating(@PathVariable String productId) {
        Double averageRating = reviewService.getAverageRating(productId);
        return ResponseEntity.ok(Map.of("averageRating", averageRating));
    }

    /**
     * Check if user can review a specific product from an order
     * GET /api/reviews/can-review?orderId={orderId}&productId={productId}
     */
    @GetMapping("/can-review")
    public ResponseEntity<Map<String, Boolean>> canReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String orderId,
            @RequestParam String productId) {
        String userId = getUserId(userDetails);
        boolean canReview = reviewService.canUserReviewOrder(userId, orderId, productId);
        return ResponseEntity.ok(Map.of("canReview", canReview));
    }

    /**
     * Get all reviewable orders for the authenticated user
     * GET /api/reviews/reviewable-orders
     */
    @GetMapping("/reviewable-orders")
    public ResponseEntity<Map<String, List<String>>> getReviewableOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = getUserId(userDetails);
        List<String> orderIds = reviewService.getReviewableOrderIds(userId);
        return ResponseEntity.ok(Map.of("orderIds", orderIds));
    }
}
