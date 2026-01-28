package com.shoppeclone.backend.promotion.service.impl;

import com.shoppeclone.backend.promotion.entity.Voucher;
import com.shoppeclone.backend.promotion.repository.VoucherRepository;
import com.shoppeclone.backend.promotion.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;

    @Override
    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    @Override
    public Voucher createVoucher(Voucher voucher) {
        return voucherRepository.save(voucher);
    }
}
