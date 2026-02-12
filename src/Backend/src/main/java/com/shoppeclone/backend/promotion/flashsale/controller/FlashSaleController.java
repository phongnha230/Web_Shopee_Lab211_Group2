package com.shoppeclone.backend.promotion.flashsale.controller;

import com.shoppeclone.backend.promotion.flashsale.dto.*;
import com.shoppeclone.backend.promotion.flashsale.entity.FlashSale;
import com.shoppeclone.backend.promotion.flashsale.entity.FlashSaleCampaign;
import com.shoppeclone.backend.promotion.flashsale.entity.FlashSaleItem;
import com.shoppeclone.backend.promotion.flashsale.service.FlashSaleService;
import com.shoppeclone.backend.shop.entity.Shop;
import com.shoppeclone.backend.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flash-sales")
@RequiredArgsConstructor
public class FlashSaleController {

    private final FlashSaleService flashSaleService;
    private final ShopService shopService;

    // --- PUBLIC ENDPOINTS ---

    @GetMapping("/current")
    public ResponseEntity<FlashSale> getCurrentFlashSale() {
        return flashSaleService.getCurrentFlashSale()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/{flashSaleId}/items")
    public ResponseEntity<List<FlashSaleItemResponse>> getFlashSaleItems(@PathVariable String flashSaleId) {
        return ResponseEntity.ok(flashSaleService.getItemsByFlashSaleId(flashSaleId));
    }

    // --- PUBLIC ENDPOINTS ---

    @GetMapping("/campaigns/open")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<FlashSaleCampaign>> getOpenCampaigns() {
        return ResponseEntity.ok(flashSaleService.getOpenCampaigns());
    }

    // --- ADMIN ENDPOINTS ---

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/campaigns")
    public ResponseEntity<FlashSaleCampaign> createCampaign(@RequestBody FlashSaleCampaignRequest request) {
        return ResponseEntity.ok(flashSaleService.createCampaign(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/campaigns")
    public ResponseEntity<List<FlashSaleCampaign>> getAllCampaigns() {
        return ResponseEntity.ok(flashSaleService.getAllCampaigns());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/campaigns/{id}")
    public ResponseEntity<FlashSaleCampaign> updateCampaign(@PathVariable String id,
            @RequestBody FlashSaleCampaignRequest request) {
        return ResponseEntity.ok(flashSaleService.updateCampaign(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/campaigns/{id}")
    public ResponseEntity<Void> deleteCampaign(@PathVariable String id) {
        flashSaleService.deleteCampaign(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/campaigns/{id}/items")
    public ResponseEntity<List<FlashSaleItemResponse>> getCampaignItems(@PathVariable String id) {
        return ResponseEntity.ok(flashSaleService.getCampaignItems(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/slots")
    public ResponseEntity<FlashSale> createSlot(@RequestBody FlashSaleSlotRequest request) {
        return ResponseEntity.ok(flashSaleService.createSlot(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/slots/{id}")
    public ResponseEntity<Void> deleteSlot(@PathVariable String id) {
        flashSaleService.deleteFlashSale(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/slots/{campaignId}")
    public ResponseEntity<List<FlashSale>> getSlotsByCampaignId(@PathVariable String campaignId) {
        return ResponseEntity.ok(flashSaleService.getSlotsByCampaignId(campaignId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/registrations/{id}/approve")
    public ResponseEntity<FlashSaleItem> approveRegistration(@PathVariable String id,
            @RequestBody ApproveRegistrationRequest request) {
        return ResponseEntity.ok(flashSaleService.approveRegistration(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/registrations/status/{status}")
    public ResponseEntity<List<FlashSaleItemResponse>> getRegistrationsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(flashSaleService.getRegistrationsByStatus(status));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/registrations/slot/{slotId}")
    public ResponseEntity<List<FlashSaleItemResponse>> getRegistrationsBySlotId(@PathVariable String slotId) {
        return ResponseEntity.ok(flashSaleService.getRegistrationsBySlotId(slotId));
    }

    // --- SHOP ENDPOINTS ---

    @PreAuthorize("hasRole('SELLER')")
    @PostMapping("/registrations")
    public ResponseEntity<FlashSaleItem> registerProduct(@RequestBody FlashSaleRegistrationRequest request,
            Authentication auth) {
        Shop shop = shopService.getMyShop(auth.getName());
        return ResponseEntity.ok(flashSaleService.registerProduct(request, shop.getId()));
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/registrations/my")
    public ResponseEntity<List<FlashSaleItem>> getMyRegistrations(Authentication auth) {
        Shop shop = shopService.getMyShop(auth.getName());
        return ResponseEntity.ok(flashSaleService.getRegistrationsByShopId(shop.getId()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/campaigns/{id}/emergency-stop")
    public ResponseEntity<Void> emergencyStopCampaign(@PathVariable String id) {
        flashSaleService.emergencyStopCampaign(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/slots/{id}/emergency-stop")
    public ResponseEntity<Void> emergencyStopSlot(@PathVariable String id) {
        flashSaleService.emergencyStopSlot(id);
        return ResponseEntity.ok().build();
    }

    // --- LEGACY/CLEANUP (Keep or replace as needed) ---

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlashSale(@PathVariable String id) {
        flashSaleService.deleteFlashSale(id);
        return ResponseEntity.noContent().build();
    }
}
