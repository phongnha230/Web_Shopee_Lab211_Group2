package com.shoppeclone.backend.promotion.flashsale.service;

import com.shoppeclone.backend.promotion.flashsale.dto.FlashSaleOrderRequest;
import com.shoppeclone.backend.promotion.flashsale.dto.FlashSaleOrderResult;
import com.shoppeclone.backend.promotion.flashsale.dto.FlashSaleStatsResponse;

import java.util.List;

public interface FlashSaleOrderService {
    /**
     * Đặt hàng Flash Sale — Atomic stock decrement dùng MongoDB findAndModify.
     * Đảm bảo không bao giờ âm kho dù 1000 request cùng lúc.
     */
    FlashSaleOrderResult placeOrder(FlashSaleOrderRequest request);

    /**
     * Lấy stats chiến dịch flash sale theo slotId — dùng cho Admin xem tất cả.
     */
    FlashSaleStatsResponse getStatsByFlashSaleId(String flashSaleId);

    /**
     * Lấy stats flash sale hiện tại theo shopId — dùng cho Seller xem của mình.
     */
    List<FlashSaleStatsResponse> getStatsByShopId(String shopId);
}
