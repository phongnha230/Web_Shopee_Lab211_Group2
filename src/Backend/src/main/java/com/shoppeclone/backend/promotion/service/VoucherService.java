package com.shoppeclone.backend.promotion.service;

import com.shoppeclone.backend.promotion.entity.Voucher;

import java.util.List;

public interface VoucherService {
    List<Voucher> getAllVouchers();

    Voucher createVoucher(Voucher voucher);
    // method to redeem/apply voucher would go here
}
