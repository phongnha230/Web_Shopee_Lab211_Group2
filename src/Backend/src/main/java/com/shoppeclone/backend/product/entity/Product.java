package com.shoppeclone.backend.product.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "products")
@Data
public class Product {
    @Id
    private String id;

    @Indexed
    private String shopId; // FK to shops.id

    private String name;
    private String description;

    // Status: ACTIVE, HIDDEN
    private ProductStatus status = ProductStatus.ACTIVE;

    // Aggregated data for display
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer totalStock;
    private Double rating = 0.0;
    private Integer sold = 0;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
