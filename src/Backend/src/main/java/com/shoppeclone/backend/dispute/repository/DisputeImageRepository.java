package com.shoppeclone.backend.dispute.repository;

import com.shoppeclone.backend.dispute.entity.DisputeImage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisputeImageRepository extends MongoRepository<DisputeImage, String> {
    List<DisputeImage> findByDisputeId(String disputeId);
}
