package com.shoppeclone.backend.product.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;

@Document(collection = "product_images")
@Data
public class ProductImage {
    @Id
    private String id;

    @Indexed
    private String productId; // FK to products.id

    private String imageUrl;
    private Integer displayOrder = 0; // For ordering images

    private LocalDateTime createdAt;
}
