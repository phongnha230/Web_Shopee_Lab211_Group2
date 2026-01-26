package com.shoppeclone.backend.shop.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateShopRequest {
    @NotBlank(message = "Tên shop không được để trống")
    private String name;

    @NotBlank(message = "Seller ID không được để trống")
    private String sellerId;

    // Pickup Address
    @NotBlank(message = "Tỉnh/Thành phố không được để trống")
    private String province;

    @NotBlank(message = "Quận/Huyện không được để trống")
    private String district;

    @NotBlank(message = "Phường/Xã không được để trống")
    private String ward;

    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    private String address;

    // Contact Information
    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    @NotBlank(message = "Email không được để trống")
    private String email;
}
