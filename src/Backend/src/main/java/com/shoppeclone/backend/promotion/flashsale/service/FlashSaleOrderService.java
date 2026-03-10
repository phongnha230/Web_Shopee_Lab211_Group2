package com.shoppeclone.backend.promotion.flashsale.service;

import com.shoppeclone.backend.promotion.flashsale.dto.FlashSaleOrderRequest;
import com.shoppeclone.backend.promotion.flashsale.dto.FlashSaleOrderResult;
import com.shoppeclone.backend.promotion.flashsale.dto.FlashSaleStatsResponse;

import java.util.List;

public interface FlashSaleOrderService {
    FlashSaleOrderResult placeOrder(FlashSaleOrderRequest request);

    FlashSaleStatsResponse getStatsByFlashSaleId(String flashSaleId);

    FlashSaleStatsResponse getStatsByCampaignId(String campaignId);

    List<FlashSaleStatsResponse> getStatsByShopId(String shopId);
}
