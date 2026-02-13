package com.shoppeclone.backend.promotion.flashsale.repository;

import com.shoppeclone.backend.promotion.flashsale.entity.FlashSaleCampaign;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlashSaleCampaignRepository extends MongoRepository<FlashSaleCampaign, String> {
    List<FlashSaleCampaign> findByStatus(String status);

    List<FlashSaleCampaign> findByStatusIn(List<String> statuses);

    long countByStatus(String status);
}
