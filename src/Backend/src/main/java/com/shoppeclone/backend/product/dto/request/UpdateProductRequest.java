package com.shoppeclone.backend.product.dto.request;

import com.shoppeclone.backend.product.entity.ProductStatus;
import lombok.Data;

import java.util.List;

@Data
public class UpdateProductRequest {
    private String name;
    private String description;
    private ProductStatus status;
    private String categoryId;
    private String shopId; // Added for admin re-assignment
    private List<String> images;

    private List<CreateProductVariantRequest> variants;

    // Flash Sale fields
    private Boolean isFlashSale;
    private java.math.BigDecimal flashSalePrice;
    private Integer flashSaleStock;
}
