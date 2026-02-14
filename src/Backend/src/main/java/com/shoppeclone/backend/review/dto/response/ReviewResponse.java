package com.shoppeclone.backend.review.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewResponse {
    private String id;
    private String userId;
    private String userName; // Tên khách hàng (fullName từ User)
    private String productId;
    private String orderId;
    private Integer rating;
    private String comment;
    private List<String> imageUrls; // URLs of review images
    private String replyComment;
    private LocalDateTime replyAt;
    private Boolean isReplyEdited;
    private LocalDateTime createdAt;
    /** true nếu reviewer đã mua sản phẩm qua đơn hàng (orderId != null) */
    private Boolean verifiedPurchase;
    private String shopId;
}
