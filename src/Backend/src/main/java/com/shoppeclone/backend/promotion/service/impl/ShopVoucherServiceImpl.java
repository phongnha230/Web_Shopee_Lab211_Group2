package com.shoppeclone.backend.promotion.service.impl;

import com.shoppeclone.backend.promotion.entity.ShopVoucher;
import com.shoppeclone.backend.promotion.repository.ShopVoucherRepository;
import com.shoppeclone.backend.promotion.service.ShopVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopVoucherServiceImpl implements ShopVoucherService {

    private final ShopVoucherRepository shopVoucherRepository;

    @Override
    public List<ShopVoucher> getVouchersByShop(String shopId) {
        return shopVoucherRepository.findByShopId(shopId);
    }

    @Override
    public ShopVoucher createShopVoucher(ShopVoucher shopVoucher) {
        return shopVoucherRepository.save(shopVoucher);
    }
}
