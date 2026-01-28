package com.shoppeclone.backend.promotion.repository;

import com.shoppeclone.backend.promotion.entity.ShopVoucher;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopVoucherRepository extends MongoRepository<ShopVoucher, String> {
    List<ShopVoucher> findByShopId(String shopId);

    Optional<ShopVoucher> findByCode(String code);
}
