package com.shoppeclone.backend.shop.service;

import com.shoppeclone.backend.shop.dto.ShopRegisterRequest;
import com.shoppeclone.backend.shop.entity.Shop;
import com.shoppeclone.backend.shop.dto.response.ShopAdminResponse;

import java.util.List;

public interface ShopService {
    Shop registerShop(String userEmail, ShopRegisterRequest request);

    Shop getMyShop(String userEmail);

    // Admin
    List<ShopAdminResponse> getPendingShops();

    List<ShopAdminResponse> getActiveShops();

    List<ShopAdminResponse> getRejectedShops();

    void approveShop(String shopId);

    void rejectShop(String shopId, String reason);

    Shop getShopById(String shopId);

    Shop updateShop(String userEmail, com.shoppeclone.backend.shop.dto.UpdateShopRequest request);
}
