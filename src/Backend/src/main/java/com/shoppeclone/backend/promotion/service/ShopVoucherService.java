package com.shoppeclone.backend.promotion.service;

import com.shoppeclone.backend.promotion.entity.ShopVoucher;

import java.util.List;

public interface ShopVoucherService {
    List<ShopVoucher> getVouchersByShop(String shopId);

    ShopVoucher createShopVoucher(ShopVoucher shopVoucher);
}
