package com.shoppeclone.backend.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemReviewInfo {
    private String variantId;  // Khớp chính xác với từng dòng đơn hàng
    private String productId;
    private boolean canReview;
    private boolean hasReviewed;
    private String reviewId; // ID of existing review (if any)
}
