package com.shoppeclone.backend.review.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "reviews")
@Data
public class Review {
    @Id
    private String id;

    @Indexed
    private String userId; // FK to users.id

    @Indexed
    private String productId; // FK to products.id

    @Indexed
    private String orderId; // FK to orders.id (only create when order is DELIVERED)

    private Integer rating; // 1-5
    private String comment;

    private List<String> imageUrls = new ArrayList<>(); // URLs of review images (max 5)

    private LocalDateTime createdAt;
}
