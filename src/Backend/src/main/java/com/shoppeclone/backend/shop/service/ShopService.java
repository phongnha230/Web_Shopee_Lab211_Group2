package com.shoppeclone.backend.shop.service;

import com.shoppeclone.backend.shop.dto.request.CreateShopRequest;
import com.shoppeclone.backend.shop.dto.request.UpdateShopRequest;
import com.shoppeclone.backend.shop.dto.response.ShopResponse;
import java.util.List;

public interface ShopService {
    ShopResponse createShop(CreateShopRequest request);

    ShopResponse getShopById(String id);

    ShopResponse getShopBySellerId(String sellerId);

    List<ShopResponse> getAllShops();

    ShopResponse updateShop(String id, UpdateShopRequest request);

    void deleteShop(String id);
}
