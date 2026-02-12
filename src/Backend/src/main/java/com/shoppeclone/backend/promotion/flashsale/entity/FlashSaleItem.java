package com.shoppeclone.backend.promotion.flashsale.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "flash_sale_items")
@Data
public class FlashSaleItem {
    @Id
    private String id;

    @Indexed
    private String flashSaleId;

    @Indexed
    private String variantId;

    @Indexed
    private String productId;

    @Indexed
    private String shopId;

    private BigDecimal salePrice;
    private Integer saleStock;
    private Integer remainingStock;

    private String status; // PENDING, APPROVED, REJECTED
    private String adminNote;

    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}
