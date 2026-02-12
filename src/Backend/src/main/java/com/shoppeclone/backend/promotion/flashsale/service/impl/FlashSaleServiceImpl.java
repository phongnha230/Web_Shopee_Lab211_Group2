package com.shoppeclone.backend.promotion.flashsale.service.impl;

import com.shoppeclone.backend.product.entity.ProductVariant;
import com.shoppeclone.backend.product.repository.ProductImageRepository;
import com.shoppeclone.backend.product.repository.ProductRepository;
import com.shoppeclone.backend.product.repository.ProductVariantRepository;
import com.shoppeclone.backend.promotion.flashsale.dto.*;
import com.shoppeclone.backend.promotion.flashsale.entity.FlashSale;
import com.shoppeclone.backend.promotion.flashsale.entity.FlashSaleCampaign;
import com.shoppeclone.backend.promotion.flashsale.entity.FlashSaleItem;
import com.shoppeclone.backend.promotion.flashsale.repository.FlashSaleCampaignRepository;
import com.shoppeclone.backend.promotion.flashsale.repository.FlashSaleItemRepository;
import com.shoppeclone.backend.promotion.flashsale.repository.FlashSaleRepository;
import com.shoppeclone.backend.promotion.flashsale.service.FlashSaleService;
import com.shoppeclone.backend.product.entity.Product;
import com.shoppeclone.backend.product.entity.ProductImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlashSaleServiceImpl implements FlashSaleService {

    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final FlashSaleCampaignRepository flashSaleCampaignRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final com.shoppeclone.backend.user.service.NotificationService notificationService;
    private final com.shoppeclone.backend.common.service.EmailService emailService;
    private final com.shoppeclone.backend.shop.service.ShopService shopService;

    @Override
    public FlashSaleCampaign createCampaign(FlashSaleCampaignRequest request) {
        FlashSaleCampaign campaign = new FlashSaleCampaign();
        campaign.setName(request.getName());
        campaign.setDescription(request.getDescription());
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        campaign.setMinDiscountPercentage(
                request.getMinDiscountPercentage() != null ? request.getMinDiscountPercentage() : 10);
        campaign.setMinStockPerProduct(request.getMinStockPerProduct() != null ? request.getMinStockPerProduct() : 5);
        campaign.setRegistrationDeadline(request.getRegistrationDeadline());
        campaign.setApprovalDeadline(request.getApprovalDeadline());
        campaign.setStatus("REGISTRATION_OPEN");
        campaign.setCreatedAt(LocalDateTime.now());
        campaign.setUpdatedAt(LocalDateTime.now());
        FlashSaleCampaign saved = flashSaleCampaignRepository.save(campaign);

        // Auto-broadcast invitation to all active shops
        try {
            broadcastCampaignInvitation(saved);
        } catch (Exception e) {
            log.error("Failed to broadcast campaign invitation: {}", e.getMessage());
        }

        return saved;
    }

    @Override
    public FlashSaleCampaign updateCampaign(String id, FlashSaleCampaignRequest request) {
        FlashSaleCampaign campaign = flashSaleCampaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
        campaign.setName(request.getName());
        campaign.setDescription(request.getDescription());
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        if (request.getMinDiscountPercentage() != null)
            campaign.setMinDiscountPercentage(request.getMinDiscountPercentage());
        if (request.getMinStockPerProduct() != null)
            campaign.setMinStockPerProduct(request.getMinStockPerProduct());
        if (request.getRegistrationDeadline() != null)
            campaign.setRegistrationDeadline(request.getRegistrationDeadline());
        if (request.getApprovalDeadline() != null)
            campaign.setApprovalDeadline(request.getApprovalDeadline());
        campaign.setUpdatedAt(LocalDateTime.now());
        return flashSaleCampaignRepository.save(campaign);
    }

    @Override
    public List<FlashSaleCampaign> getAllCampaigns() {
        return flashSaleCampaignRepository.findAll();
    }

    @Override
    public List<FlashSaleCampaign> getOpenCampaigns() {
        return flashSaleCampaignRepository.findByStatus("REGISTRATION_OPEN");
    }

    @Override
    @Transactional
    public void deleteCampaign(String id) {
        // Find all slots for this campaign
        List<FlashSale> slots = flashSaleRepository.findByCampaignId(id);
        for (FlashSale slot : slots) {
            // Delete items for each slot
            flashSaleItemRepository.deleteByFlashSaleId(slot.getId());
            // Delete slot
            flashSaleRepository.delete(slot);
        }
        // Delete campaign
        flashSaleCampaignRepository.deleteById(id);
    }

    @Override
    public FlashSale createSlot(FlashSaleSlotRequest request) {
        FlashSale slot = new FlashSale();
        slot.setCampaignId(request.getCampaignId());
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setStatus("ACTIVE");
        return flashSaleRepository.save(slot);
    }

    @Override
    public List<FlashSale> getSlotsByCampaignId(String campaignId) {
        return flashSaleRepository.findByCampaignId(campaignId);
    }

    @Override
    public FlashSaleItem registerProduct(FlashSaleRegistrationRequest request, String shopId) {
        // Fetch Slot and Campaign to get dynamic rules
        FlashSale slot = flashSaleRepository.findById(request.getSlotId())
                .orElseThrow(() -> new RuntimeException("Flash sale slot not found"));
        FlashSaleCampaign campaign = flashSaleCampaignRepository.findById(slot.getCampaignId())
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        ProductVariant variant = productVariantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new RuntimeException("Product variant not found"));

        // Dynamic Price Guard logic
        double minDiscount = campaign.getMinDiscountPercentage() / 100.0;
        BigDecimal maxAllowedPrice = variant.getPrice().multiply(new BigDecimal(1.0 - minDiscount));

        if (request.getSalePrice().compareTo(maxAllowedPrice) > 0) {
            throw new RuntimeException(
                    String.format("Sale price must be at least %d%% lower than current price (Max: %s)",
                            campaign.getMinDiscountPercentage(), maxAllowedPrice.toString()));
        }

        // Dynamic Stock validation
        if (request.getSaleStock() < campaign.getMinStockPerProduct()) {
            throw new RuntimeException(
                    String.format("Min stock required for this campaign is %d", campaign.getMinStockPerProduct()));
        }

        FlashSaleItem registration = new FlashSaleItem();
        registration.setFlashSaleId(request.getSlotId());
        registration.setProductId(request.getProductId());
        registration.setVariantId(request.getVariantId());
        registration.setShopId(shopId);
        registration.setSalePrice(request.getSalePrice());
        registration.setSaleStock(request.getSaleStock());
        registration.setRemainingStock(request.getSaleStock());
        registration.setStatus("PENDING");
        registration.setCreatedAt(LocalDateTime.now());
        registration.setUpdatedAt(LocalDateTime.now());

        return flashSaleItemRepository.save(registration);
    }

    @Override
    public List<FlashSaleItem> getRegistrationsByShopId(String shopId) {
        return flashSaleItemRepository.findByShopId(shopId);
    }

    @Override
    public List<FlashSaleItemResponse> getCampaignItems(String campaignId) {
        List<FlashSale> slots = flashSaleRepository.findByCampaignId(campaignId);
        List<String> slotIds = slots.stream().map(FlashSale::getId).collect(Collectors.toList());
        List<FlashSaleItem> allItems = flashSaleItemRepository.findAll().stream()
                .filter(item -> slotIds.contains(item.getFlashSaleId()) && "APPROVED".equals(item.getStatus()))
                .collect(Collectors.toList());

        return allItems.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FlashSaleItem approveRegistration(String registrationId, ApproveRegistrationRequest request) {
        FlashSaleItem registration = flashSaleItemRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found"));

        com.shoppeclone.backend.shop.entity.Shop shop = shopService.getShopById(registration.getShopId());
        Product product = productRepository.findById(registration.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if ("APPROVED".equals(request.getStatus())) {
            // Inventory Locking Logic
            ProductVariant variant = productVariantRepository.findById(registration.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found"));

            if (variant.getStock() < registration.getSaleStock()) {
                throw new RuntimeException("Not enough stock in shop inventory. Available: " + variant.getStock());
            }

            variant.setStock(variant.getStock() - registration.getSaleStock());
            productVariantRepository.save(variant);

            registration.setStatus("APPROVED");

            // Notifications
            notificationService.createNotification(shop.getOwnerId(),
                    "Flash Sale Approved! ⚡",
                    "Your product '" + product.getName() + "' has been approved for Flash Sale.",
                    "FLASH_SALE");

            try {
                FlashSale slot = flashSaleRepository.findById(registration.getFlashSaleId()).orElse(null);
                String startTime = (slot != null) ? slot.getStartTime().toString() : "N/A";
                emailService.sendFlashSaleApprovalEmail(shop.getEmail(), product.getName(), startTime);
            } catch (Exception e) {
                System.err.println("Failed to send approval email: " + e.getMessage());
            }
        } else {
            registration.setStatus("REJECTED");

            // Notifications
            notificationService.createNotification(shop.getOwnerId(),
                    "Flash Sale Rejected ❌",
                    "Your Flash Sale registration for '" + product.getName() + "' was rejected. Reason: "
                            + request.getAdminNote(),
                    "FLASH_SALE");

            try {
                emailService.sendFlashSaleRejectionEmail(shop.getEmail(), product.getName(), request.getAdminNote());
            } catch (Exception e) {
                System.err.println("Failed to send rejection email: " + e.getMessage());
            }
        }

        registration.setAdminNote(request.getAdminNote());
        return flashSaleItemRepository.save(registration);
    }

    @Override
    public FlashSale createFlashSale(FlashSale flashSale) {
        return flashSaleRepository.save(flashSale);
    }

    @Override
    public FlashSale updateFlashSale(String id, FlashSale flashSale) {
        FlashSale existing = flashSaleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flash sale not found: " + id));
        if (flashSale.getStartTime() != null)
            existing.setStartTime(flashSale.getStartTime());
        if (flashSale.getEndTime() != null)
            existing.setEndTime(flashSale.getEndTime());
        return flashSaleRepository.save(existing);
    }

    @Override
    public void deleteFlashSale(String id) {
        flashSaleItemRepository.deleteByFlashSaleId(id);
        flashSaleRepository.deleteById(id);
    }

    @Override
    public List<FlashSale> getActiveFlashSales() {
        return flashSaleRepository.findByStartTimeAfter(LocalDateTime.now().minusDays(1));
    }

    @Override
    public Optional<FlashSale> getCurrentFlashSale() {
        LocalDateTime now = LocalDateTime.now();
        List<FlashSale> all = flashSaleRepository.findAll();
        return all.stream()
                .filter(fs -> fs.getStartTime() != null && fs.getEndTime() != null)
                .filter(fs -> !fs.getStartTime().isAfter(now) && !fs.getEndTime().isBefore(now))
                .findFirst();
    }

    @Override
    public List<FlashSaleItemResponse> getRegistrationsByStatus(String status) {
        return flashSaleItemRepository.findByStatus(status).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FlashSaleItemResponse> getRegistrationsBySlotId(String slotId) {
        return flashSaleItemRepository.findByFlashSaleIdAndStatus(slotId, "APPROVED").stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FlashSaleItemResponse> getItemsByFlashSaleId(String flashSaleId) {
        return flashSaleItemRepository.findByFlashSaleIdAndStatus(flashSaleId, "APPROVED").stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void emergencyStopCampaign(String campaignId) {
        FlashSaleCampaign campaign = flashSaleCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        campaign.setStatus("FINISHED");
        flashSaleCampaignRepository.save(campaign);
        log.info("Emergency Stop triggered for Campaign: {}", campaign.getName());

        List<FlashSale> slots = flashSaleRepository.findByCampaignId(campaignId);
        for (FlashSale slot : slots) {
            emergencyStopSlot(slot.getId());
        }
    }

    @Override
    @Transactional
    public void emergencyStopSlot(String slotId) {
        FlashSale slot = flashSaleRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        slot.setStatus("INACTIVE");
        flashSaleRepository.save(slot);
        log.info("Emergency Stop triggered for Slot: {}", slotId);

        List<FlashSaleItem> items = flashSaleItemRepository.findByFlashSaleIdAndStatus(slotId, "APPROVED");
        for (FlashSaleItem item : items) {
            // Revert Product
            productRepository.findById(item.getProductId()).ifPresent(p -> {
                p.setIsFlashSale(false);
                productRepository.save(p);
            });

            // Restore Stock
            productVariantRepository.findById(item.getVariantId()).ifPresent(v -> {
                if (item.getRemainingStock() != null && item.getRemainingStock() > 0) {
                    v.setStock(v.getStock() + item.getRemainingStock());
                    productVariantRepository.save(v);
                }
            });

            // Notify Shop
            notificationService.createNotification(item.getShopId(),
                    "Emergency Stop ⚠️",
                    "Your flash sale item was halted due to an emergency stop on the campaign/slot.",
                    "FLASH_SALE");
        }
    }

    private FlashSaleItemResponse convertToResponse(FlashSaleItem item) {
        FlashSaleItemResponse resp = new FlashSaleItemResponse();
        resp.setId(item.getId());
        resp.setFlashSaleId(item.getFlashSaleId());
        resp.setProductId(item.getProductId());
        resp.setVariantId(item.getVariantId());
        resp.setSalePrice(item.getSalePrice());
        resp.setSaleStock(item.getSaleStock());
        resp.setRemainingStock(item.getRemainingStock());
        resp.setStatus(item.getStatus());

        // Fetch Product Info
        productRepository.findById(item.getProductId()).ifPresent(p -> {
            resp.setProductName(p.getName());
            // Fetch First Image
            productImageRepository.findByProductIdOrderByDisplayOrderAsc(p.getId()).stream()
                    .findFirst()
                    .ifPresent(img -> resp.setProductImage(img.getImageUrl()));
        });

        // Fetch Variant Info
        productVariantRepository.findById(item.getVariantId()).ifPresent(v -> {
            resp.setVariantName(
                    (v.getColor() != null ? v.getColor() : "") + " " + (v.getSize() != null ? v.getSize() : ""));
            resp.setOriginalPrice(v.getPrice());
        });

        return resp;
    }

    @Override
    @Transactional
    public void broadcastCampaignInvitation(FlashSaleCampaign campaign) {
        log.info("Broadcasting invitation for campaign: {}", campaign.getName());

        List<com.shoppeclone.backend.shop.dto.response.ShopAdminResponse> activeShops = shopService.getActiveShops();
        String regDeadline = campaign.getRegistrationDeadline() != null ? campaign.getRegistrationDeadline().toString()
                : "Open";

        for (com.shoppeclone.backend.shop.dto.response.ShopAdminResponse shopResp : activeShops) {
            // Send In-app Notification
            notificationService.createNotification(
                    shopResp.getOwnerId(),
                    "New Flash Sale Campaign! ⚡",
                    "Campaign '" + campaign.getName() + "' is now open for registration. Join now to boost your sales!",
                    "FLASH_SALE");

            // Send Email
            try {
                emailService.sendCampaignInvitationEmail(shopResp.getEmail(), campaign.getName(), regDeadline);
            } catch (Exception e) {
                log.warn("Failed to send invitation email to {}: {}", shopResp.getEmail(), e.getMessage());
            }
        }
    }
}
