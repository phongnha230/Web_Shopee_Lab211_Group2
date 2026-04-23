package com.shoppeclone.backend.auth.repository;

import com.shoppeclone.backend.auth.model.RateLimitRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RateLimitRecordRepository extends MongoRepository<RateLimitRecord, String> {
}
