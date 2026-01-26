package com.shoppeclone.backend.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCategoryRequest {
    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;

    private String parentId; // Optional for hierarchical categories
}
