package com.shoppeclone.backend.product.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "product_variants")
@Data
public class ProductVariant {
    @Id
    private String id;

    @Indexed
    private String productId; // FK to products.id

    private String size;
    private String color;
    private BigDecimal price;
    private Integer stock = 0;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
