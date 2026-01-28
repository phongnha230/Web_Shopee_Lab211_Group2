package com.shoppeclone.backend.promotion.repository;

import com.shoppeclone.backend.promotion.entity.Voucher;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoucherRepository extends MongoRepository<Voucher, String> {
    Optional<Voucher> findByCode(String code);
}
