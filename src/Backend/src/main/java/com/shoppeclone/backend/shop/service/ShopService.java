package com.shoppeclone.backend.shop.service;

import com.shoppeclone.backend.shop.dto.ShopRegisterRequest;
import com.shoppeclone.backend.shop.entity.Shop;

import java.util.List;

public interface ShopService {
    Shop registerShop(String userEmail, ShopRegisterRequest request);

    Shop getMyShop(String userEmail);

    // Admin
    List<Shop> getPendingShops();

    List<Shop> getActiveShops();

    List<Shop> getRejectedShops();

    void approveShop(String shopId);

    void rejectShop(String shopId, String reason);

    Shop getShopById(String shopId);

    Shop updateShop(String userEmail, com.shoppeclone.backend.shop.dto.UpdateShopRequest request);
}
