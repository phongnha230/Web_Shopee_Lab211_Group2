package com.shoppeclone.backend.shop.dto.request;

import lombok.Data;

@Data
public class UpdateShopRequest {
    private String name;

    // Address fields (optional for update)
    private String province;
    private String district;
    private String ward;
    private String address;

    // Contact fields (optional for update)
    private String phone;
    private String email;

    private com.shoppeclone.backend.shop.entity.ShopStatus status;
}
