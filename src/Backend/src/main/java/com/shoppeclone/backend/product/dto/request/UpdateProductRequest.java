package com.shoppeclone.backend.product.dto.request;

import com.shoppeclone.backend.product.entity.ProductStatus;
import lombok.Data;

@Data
public class UpdateProductRequest {
    private String name;
    private String description;
    private ProductStatus status;
}
