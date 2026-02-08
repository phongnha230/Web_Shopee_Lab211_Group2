package com.shoppeclone.backend.shipping.service.impl;

import com.shoppeclone.backend.shipping.entity.ShippingProvider;
import com.shoppeclone.backend.shipping.repository.ShippingProviderRepository;
import com.shoppeclone.backend.shipping.service.ShippingService;
import com.shoppeclone.backend.user.model.Address;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {

    private final ShippingProviderRepository shippingProviderRepository;

    @Override
    public List<ShippingProvider> getAllProviders() {
        return shippingProviderRepository.findAll();
    }

    @Override
    public BigDecimal calculateShippingFee(String providerId, Address address, BigDecimal orderTotal) {
        // Free shipping for orders >= 1,000,000 VND
        if (orderTotal.compareTo(new BigDecimal("1000000")) >= 0) {
            return BigDecimal.ZERO;
        }

        // Standard fee logic based on provider
        if ("standard".equals(providerId)) {
            return new BigDecimal("15000");
        } else if ("express".equals(providerId)) {
            return new BigDecimal("30000");
        }

        // Default fallback
        return new BigDecimal("30000");
    }
}
