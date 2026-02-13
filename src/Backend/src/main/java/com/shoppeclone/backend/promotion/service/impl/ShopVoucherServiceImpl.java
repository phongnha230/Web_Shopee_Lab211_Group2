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

    @Override
    public ShopVoucher updateShopVoucher(String id, ShopVoucher shopVoucher) {
        ShopVoucher existingVoucher = shopVoucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại: " + id));

        // Cập nhật các trường
        existingVoucher.setCode(shopVoucher.getCode());
        existingVoucher.setDiscount(shopVoucher.getDiscount());
        existingVoucher.setQuantity(shopVoucher.getQuantity());
        existingVoucher.setMinSpend(shopVoucher.getMinSpend());
        existingVoucher.setStartDate(shopVoucher.getStartDate());
        existingVoucher.setEndDate(shopVoucher.getEndDate());

        return shopVoucherRepository.save(existingVoucher);
    }

    @Override
    public ShopVoucher getVoucherByCode(String code) {
        return shopVoucherRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Shop Voucher not found: " + code));
    }

    @Override
    public void deleteShopVoucher(String id) {
        shopVoucherRepository.deleteById(id);
    }
}
