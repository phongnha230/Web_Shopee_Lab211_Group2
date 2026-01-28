package com.shoppeclone.backend.review.service.impl;

import com.shoppeclone.backend.review.dto.request.CreateReviewRequest;
import com.shoppeclone.backend.review.dto.request.UpdateReviewRequest;
import com.shoppeclone.backend.review.dto.response.ReviewResponse;
import com.shoppeclone.backend.review.entity.Review;
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

    @Override
    public ReviewResponse createReview(CreateReviewRequest request) {
        Review review = new Review();
        review.setUserId(request.getUserId());
        review.setProductId(request.getProductId());
        review.setOrderId(request.getOrderId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
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

    private ReviewResponse mapToResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setUserId(review.getUserId());
        response.setProductId(review.getProductId());
        response.setOrderId(review.getOrderId());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }
}
