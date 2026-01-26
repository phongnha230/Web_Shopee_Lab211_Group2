package com.shoppeclone.backend.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateProductRequest {
    @NotBlank(message = "ID shop không được để trống")
    private String shopId;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    private String description;
}
