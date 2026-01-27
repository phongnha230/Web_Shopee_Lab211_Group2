package com.shoppeclone.backend.shipping.service;

import com.shoppeclone.backend.shipping.entity.ShippingProvider;
import com.shoppeclone.backend.user.model.Address;

import java.math.BigDecimal;
import java.util.List;

public interface ShippingService {
    List<ShippingProvider> getAllProviders();

    BigDecimal calculateShippingFee(String providerId, Address address, BigDecimal orderTotal);
}
