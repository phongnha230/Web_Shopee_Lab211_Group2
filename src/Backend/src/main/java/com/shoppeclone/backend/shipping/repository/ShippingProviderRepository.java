package com.shoppeclone.backend.shipping.repository;

import com.shoppeclone.backend.shipping.entity.ShippingProvider;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ShippingProviderRepository extends MongoRepository<ShippingProvider, String> {
}
