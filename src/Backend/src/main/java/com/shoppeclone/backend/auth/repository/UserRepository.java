package com.shoppeclone.backend.auth.repository;

import com.shoppeclone.backend.auth.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRolesName(String roleName);

    java.util.List<User> findByRolesName(String roleName);
}