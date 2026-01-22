package com.shoppeclone.backend.auth.repository;

import com.shoppeclone.backend.auth.model.OAuthAccount;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface OAuthAccountRepository extends MongoRepository<OAuthAccount, String> {
    Optional<OAuthAccount> findByProviderAndProviderId(String provider, String providerId);

    Optional<OAuthAccount> findByProviderAndEmail(String provider, String email);
}