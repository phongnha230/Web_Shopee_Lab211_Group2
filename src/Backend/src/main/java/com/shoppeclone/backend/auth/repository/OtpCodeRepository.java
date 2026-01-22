package com.shoppeclone.backend.auth.repository;

import com.shoppeclone.backend.auth.model.OtpCode;
import com.shoppeclone.backend.auth.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface OtpCodeRepository extends MongoRepository<OtpCode, String> {
    Optional<OtpCode> findByUserAndCodeAndTypeAndUsed(User user, String code, String type, boolean used);

    void deleteByUser(User user);
}