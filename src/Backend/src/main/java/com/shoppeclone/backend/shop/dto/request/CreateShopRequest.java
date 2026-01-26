package com.shoppeclone.backend.shop.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateShopRequest {
    @NotBlank(message = "Tên shop không được để trống")
    private String name;

    @NotBlank(message = "Seller ID không được để trống")
    private String sellerId;
}
