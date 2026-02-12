package com.shoppeclone.backend.promotion.flashsale.service;

import com.shoppeclone.backend.promotion.flashsale.dto.*;
import com.shoppeclone.backend.promotion.flashsale.entity.FlashSale;
import com.shoppeclone.backend.promotion.flashsale.entity.FlashSaleCampaign;
import com.shoppeclone.backend.promotion.flashsale.entity.FlashSaleItem;

import java.util.List;
import java.util.Optional;

public interface FlashSaleService {
    // Campaign Management (Admin)
    FlashSaleCampaign createCampaign(FlashSaleCampaignRequest request);

    FlashSaleCampaign updateCampaign(String id, FlashSaleCampaignRequest request);

    List<FlashSaleCampaign> getAllCampaigns();

    List<FlashSaleCampaign> getOpenCampaigns();

    void deleteCampaign(String id);

    List<FlashSaleItemResponse> getCampaignItems(String campaignId);

    // Slot Management (Admin)
    FlashSale createSlot(FlashSaleSlotRequest request);

    List<FlashSale> getSlotsByCampaignId(String campaignId);

    // Registration Management (Shop)
    FlashSaleItem registerProduct(FlashSaleRegistrationRequest request, String shopId);

    List<FlashSaleItem> getRegistrationsByShopId(String shopId);

    // Curation (Admin)
    FlashSaleItem approveRegistration(String registrationId, ApproveRegistrationRequest request);

    List<FlashSaleItemResponse> getRegistrationsByStatus(String status);

    List<FlashSaleItemResponse> getRegistrationsBySlotId(String slotId);

    // Legacy/Public methods
    FlashSale createFlashSale(FlashSale flashSale);

    FlashSale updateFlashSale(String id, FlashSale flashSale);

    void deleteFlashSale(String id);

    List<FlashSale> getActiveFlashSales();

    Optional<FlashSale> getCurrentFlashSale();

    List<FlashSaleItemResponse> getItemsByFlashSaleId(String flashSaleId);

    void emergencyStopCampaign(String campaignId);

    void emergencyStopSlot(String slotId);

    void broadcastCampaignInvitation(FlashSaleCampaign campaign);
}
