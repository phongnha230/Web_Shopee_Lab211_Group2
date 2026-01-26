package com.shoppeclone.backend.shop.dto.request;

import lombok.Data;

@Data
public class UpdateShopRequest {
    private String name;
    private com.shoppeclone.backend.shop.entity.ShopStatus status;
}
