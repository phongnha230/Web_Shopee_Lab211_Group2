package com.shoppeclone.backend.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateReviewRequest {

    // userId is set by backend from authenticated user - do not send from client
    private String userId;

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    private String comment;

    @Size(max = 5, message = "Maximum 5 images allowed per review")
    private List<String> imageUrls; // Optional: URLs of uploaded images (max 5)
}
