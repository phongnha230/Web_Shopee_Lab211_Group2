package com.shoppeclone.backend.promotion.service.impl;

import com.shoppeclone.backend.order.entity.Order;
import com.shoppeclone.backend.order.repository.OrderRepository;
import com.shoppeclone.backend.promotion.entity.Voucher;
import com.shoppeclone.backend.promotion.repository.VoucherRepository;
import com.shoppeclone.backend.promotion.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final OrderRepository orderRepository;

    @Override
    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    @Override
    public Voucher createVoucher(Voucher voucher) {
        return voucherRepository.save(voucher);
    }

    @Override
    public Voucher getVoucherByCode(String code) {
        return voucherRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Voucher not found: " + code));
    }

    @Override
    public List<String> getUsedVoucherCodesByUserId(String userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        Set<String> used = new HashSet<>();
        for (Order o : orders) {
            if (o.getVoucherCode() != null && !o.getVoucherCode().trim().isEmpty()) {
                used.add(o.getVoucherCode().trim());
            }
            if (o.getShippingVoucherCode() != null && !o.getShippingVoucherCode().trim().isEmpty()) {
                used.add(o.getShippingVoucherCode().trim());
            }
        }
        return new ArrayList<>(used);
    }
}
