package com.shoppeclone.backend.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateProductVariantRequest {
    @NotBlank(message = "ID sản phẩm không được để trống")
    private String productId;

    private String size;
    private String color;

    @NotNull(message = "Giá không được để trống")
    private BigDecimal price;

    @NotNull(message = "Số lượng không được để trống")
    private Integer stock;
}
