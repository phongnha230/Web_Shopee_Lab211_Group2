package com.shoppeclone.backend.auth.repository;

import com.shoppeclone.backend.auth.model.UserSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserSessionRepository extends MongoRepository<UserSession, String> {
    Optional<UserSession> findByRefreshToken(String refreshToken);
    void deleteByRefreshToken(String refreshToken);
}