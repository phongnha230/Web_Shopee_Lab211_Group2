package com.shoppeclone.backend.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class CreateProductRequest {
    @NotBlank(message = "ID shop không được để trống")
    private String shopId;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    private String description;

    // New fields for One-Shot Creation
    private List<CreateProductVariantRequest> variants;
    private List<String> images; // List of Image URLs
    private String categoryId;

    // Flash Sale fields
    private Boolean isFlashSale;
    private java.math.BigDecimal flashSalePrice;
    private Integer flashSaleStock;
}
