package com.shoppeclone.backend.product.dto.response;

import com.shoppeclone.backend.product.entity.ProductStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductResponse {
    private String id;
    private String shopId;
    private String name;
    private String description;
    private ProductStatus status;
    private List<String> categories; // Category IDs
    private List<ProductVariantResponse> variants;
    private List<String> images; // Image URLs
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Aggregated fields
    private java.math.BigDecimal minPrice;
    private java.math.BigDecimal maxPrice;
    private Integer totalStock;
    private Integer sold;
    private Double rating;
    private Integer reviewCount;

    // Flash Sale fields
    private Boolean isFlashSale;
    private java.math.BigDecimal flashSalePrice;
    private Integer flashSaleStock;
    private Integer flashSaleSold;
    private LocalDateTime flashSaleEndTime;
}
