package com.shoppeclone.backend.review.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private String id;
    private String userId;
    private String productId;
    private String orderId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
