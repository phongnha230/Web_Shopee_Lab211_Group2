package com.shoppeclone.backend.shop.dto;

import lombok.Data;

@Data
public class UpdateShopRequest {
    private String name;
    private String description;
    private String address;
    private String phone;
    private String email;
    private String logo;
}
