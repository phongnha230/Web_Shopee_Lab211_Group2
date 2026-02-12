package com.shoppeclone.backend.promotion.flashsale.repository;

import com.shoppeclone.backend.promotion.flashsale.entity.FlashSale;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlashSaleRepository extends MongoRepository<FlashSale, String> {
    List<FlashSale> findByStartTimeAfter(LocalDateTime time);

    List<FlashSale> findByCampaignId(String campaignId);

    /** Flash sale đang diễn ra: startTime <= now <= endTime */
    List<FlashSale> findByStartTimeLessThanEqualAndEndTimeGreaterThanEqual(LocalDateTime now, LocalDateTime nowEnd);
}
