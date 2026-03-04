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

import java.util.Comparator;
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
        validateCampaignTimeline(request, null);

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
        validateCampaignTimeline(request, campaign);

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

    private void validateCampaignTimeline(FlashSaleCampaignRequest request, FlashSaleCampaign existingCampaign) {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime startDate = request.getStartDate() != null ? request.getStartDate()
                : existingCampaign != null ? existingCampaign.getStartDate() : null;
        LocalDateTime endDate = request.getEndDate() != null ? request.getEndDate()
                : existingCampaign != null ? existingCampaign.getEndDate() : null;
        LocalDateTime registrationDeadline = request.getRegistrationDeadline() != null
                ? request.getRegistrationDeadline()
                : existingCampaign != null ? existingCampaign.getRegistrationDeadline() : null;
        LocalDateTime approvalDeadline = request.getApprovalDeadline() != null ? request.getApprovalDeadline()
                : existingCampaign != null ? existingCampaign.getApprovalDeadline() : null;

        if (startDate == null || endDate == null) {
            throw new RuntimeException("Campaign start/end date is required.");
        }
        if (!endDate.isAfter(startDate)) {
            throw new RuntimeException("Campaign end date must be after start date.");
        }

        if (registrationDeadline != null) {
            LocalDateTime oldRegistrationDeadline = existingCampaign != null
                    ? existingCampaign.getRegistrationDeadline()
                    : null;
            boolean isUpdatedRegistrationDeadline = existingCampaign == null
                    || oldRegistrationDeadline == null
                    || !registrationDeadline.equals(oldRegistrationDeadline);

            if (isUpdatedRegistrationDeadline && registrationDeadline.isBefore(now)) {
                throw new RuntimeException("Registration deadline must be in the future (Vietnam time).");
            }
            if (registrationDeadline.isAfter(startDate)) {
                throw new RuntimeException("Registration deadline must be on or before campaign start date.");
            }
        }

        if (approvalDeadline != null) {
            if (approvalDeadline.isAfter(startDate)) {
                throw new RuntimeException("Approval deadline must be on or before campaign start date.");
            }
            if (registrationDeadline != null && approvalDeadline.isBefore(registrationDeadline)) {
                throw new RuntimeException("Approval deadline must be after or equal to registration deadline.");
            }
        }
    }

    @Override
    public List<FlashSaleCampaign> getAllCampaigns() {
        return flashSaleCampaignRepository.findAll();
    }

    @Override
    public List<FlashSaleCampaign> getOpenCampaigns() {
        LocalDateTime now = LocalDateTime.now();

        return flashSaleCampaignRepository.findAll().stream()
                .filter(campaign -> {
                    String status = campaign.getStatus() != null ? campaign.getStatus().toUpperCase() : "";

                    // Only hide campaigns that are truly FINISHED (admin-confirmed end or all slots
                    // done).
                    // Campaigns where registration deadline has passed should still be visible to
                    // sellers
                    // as "Registration Closed" — they are only removed when admin explicitly
                    // deletes them.
                    if ("FINISHED".equals(status)) {
                        return false;
                    }

                    // Show REGISTRATION_OPEN or ONGOING campaigns
                    if ("REGISTRATION_OPEN".equals(status) || "ONGOING".equals(status)) {
                        return true;
                    }

                    // Fallback: if campaign status is stale but has a usable slot, still expose to
                    // seller.
                    List<FlashSale> slots = flashSaleRepository.findByCampaignId(campaign.getId());
                    return slots.stream().anyMatch(slot -> {
                        String slotStatus = slot.getStatus() != null ? slot.getStatus().toUpperCase() : "";
                        boolean slotOpen = "ACTIVE".equals(slotStatus) || "ONGOING".equals(slotStatus);
                        return slotOpen
                                && slot.getEndTime() != null
                                && slot.getEndTime().isAfter(now);
                    });
                })
                .collect(Collectors.toList());
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
        FlashSaleCampaign campaign = flashSaleCampaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        LocalDateTime now = LocalDateTime.now();
        log.info("[createSlot] VN now = {}, slotStart = {}, slotEnd = {}, campaignStatus = {}, regDeadline = {}",
                now, request.getStartTime(), request.getEndTime(), campaign.getStatus(),
                campaign.getRegistrationDeadline());

        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new RuntimeException("Cannot add slot: start/end time is required.");
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new RuntimeException("Cannot add slot: end time must be after start time.");
        }
        if (campaign.getRegistrationDeadline() != null && now.isAfter(campaign.getRegistrationDeadline())) {
            log.warn("[createSlot] BLOCKED: registration deadline passed. now={} > deadline={}", now,
                    campaign.getRegistrationDeadline());
            throw new RuntimeException("Cannot add slot: registration deadline has passed.");
        }
        if (request.getEndTime() != null && !request.getEndTime().isAfter(now)) {
            log.warn("[createSlot] BLOCKED: slot end time not in future. endTime={} <= now={}", request.getEndTime(),
                    now);
            throw new RuntimeException("Cannot add slot: slot end time must be in the future.");
        }

        FlashSale slot = new FlashSale();
        slot.setCampaignId(request.getCampaignId());
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setStatus("ACTIVE");
        FlashSale savedSlot = flashSaleRepository.save(slot);
        log.info("[createSlot] Slot saved successfully: id={}", savedSlot.getId());

        // Re-open stale campaign status so seller can see/register after admin adds
        // slot.
        String status = campaign.getStatus() != null ? campaign.getStatus().toUpperCase() : "";
        if ("FINISHED".equals(status)) {
            campaign.setStatus("REGISTRATION_OPEN");
            campaign.setUpdatedAt(now);
            flashSaleCampaignRepository.save(campaign);
            log.info(
                    "[createSlot] Campaign re-opened from FINISHED to REGISTRATION_OPEN (no broadcast — admin must trigger manually)");
        }

        return savedSlot;
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

        // --- DEADLINE VALIDATION ---
        LocalDateTime now = LocalDateTime.now();

        // Only check registration deadline (not status, because campaign may
        // auto-transition to ONGOING while deadline is still valid)
        if (campaign.getRegistrationDeadline() != null && now.isAfter(campaign.getRegistrationDeadline())) {
            throw new RuntimeException(
                    "Registration deadline has passed. Deadline was: " + campaign.getRegistrationDeadline()
                            + " (Vietnam time)");
        }
        // --- END VALIDATION ---

        ProductVariant variant;
        if (request.getVariantId() != null) {
            variant = productVariantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Product variant not found"));
        } else {
            // Attempt to find the default (first) variant for the product
            List<ProductVariant> variants = productVariantRepository.findByProductId(request.getProductId());
            if (variants.isEmpty()) {
                throw new RuntimeException(
                        "Cannot register a product without inventory records (Variants). Please add a variant to your product.");
            }
            variant = variants.get(0);
            request.setVariantId(variant.getId()); // Update request for later saving
        }

        // Dynamic Price Guard logic
        BigDecimal minDiscount = new BigDecimal(campaign.getMinDiscountPercentage()).divide(new BigDecimal(100));
        BigDecimal maxAllowedPrice = variant.getPrice().multiply(BigDecimal.ONE.subtract(minDiscount));

        if (request.getSalePrice().compareTo(maxAllowedPrice) > 0) {
            throw new RuntimeException(
                    String.format("Sale price must be at least %d%% lower than current price (Max: %s)",
                            campaign.getMinDiscountPercentage(), maxAllowedPrice.stripTrailingZeros().toPlainString()));
        }

        // Dynamic Stock validation
        // 1. Check against Campaign Min Requirement
        if (request.getSaleStock() < campaign.getMinStockPerProduct()) {
            throw new RuntimeException(
                    String.format("Stock must be at least %d items (Admin Min Stock Requirement)",
                            campaign.getMinStockPerProduct()));
        }
        // 2. Check against Actual Shop Inventory
        if (request.getSaleStock() > variant.getStock()) {
            throw new RuntimeException(
                    String.format("You cannot register more than your current stock (%d items)", variant.getStock()));
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

        // Inventory Locking: Deduct from base stock
        variant.setStock(variant.getStock() - request.getSaleStock());
        productVariantRepository.save(variant);

        // Sync Parent Product Total Stock
        syncProductTotalStock(registration.getProductId());

        return flashSaleItemRepository.save(registration);
    }

    private void syncProductTotalStock(String productId) {
        productRepository.findById(productId).ifPresent(product -> {
            List<ProductVariant> variants = productVariantRepository.findByProductId(productId);
            int total = variants.stream().mapToInt(ProductVariant::getStock).sum();
            product.setTotalStock(total);
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);
        });
    }

    @Override
    public List<FlashSaleItem> getRegistrationsByShopId(String shopId) {
        List<FlashSaleItem> registrations = flashSaleItemRepository.findByShopId(shopId);
        registrations.sort((a, b) -> {
            java.util.Map<String, Integer> statusPriority = java.util.Map.of(
                    "PENDING", 0,
                    "APPROVED", 1,
                    "REJECTED", 2);
            String aStatus = a != null && a.getStatus() != null ? a.getStatus().toUpperCase() : "";
            String bStatus = b != null && b.getStatus() != null ? b.getStatus().toUpperCase() : "";
            int aPriority = statusPriority.getOrDefault(aStatus, 99);
            int bPriority = statusPriority.getOrDefault(bStatus, 99);
            if (aPriority != bPriority) {
                return Integer.compare(aPriority, bPriority);
            }

            java.time.LocalDateTime aTime = a != null ? a.getCreatedAt() : null;
            java.time.LocalDateTime bTime = b != null ? b.getCreatedAt() : null;

            if (aTime == null && bTime == null) {
                String aId = (a != null && a.getId() != null) ? a.getId() : "";
                String bId = (b != null && b.getId() != null) ? b.getId() : "";
                return bId.compareTo(aId);
            }
            if (aTime == null)
                return 1;
            if (bTime == null)
                return -1;

            int timeCompare = bTime.compareTo(aTime); // newest first
            if (timeCompare != 0)
                return timeCompare;

            String aId = (a != null && a.getId() != null) ? a.getId() : "";
            String bId = (b != null && b.getId() != null) ? b.getId() : "";
            return bId.compareTo(aId);
        });
        return registrations;
    }

    @Override
    public List<FlashSaleItemResponse> getRegistrationDetailsByShopId(String shopId) {
        return getRegistrationsByShopId(shopId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FlashSaleItemResponse> getCampaignItems(String campaignId) {
        List<String> slotIds = flashSaleRepository.findByCampaignId(campaignId).stream()
                .map(FlashSale::getId)
                .collect(Collectors.toList());

        if (slotIds.isEmpty()) {
            return List.of();
        }

        java.util.Map<String, Integer> statusPriority = java.util.Map.of(
                "APPROVED", 0,
                "PENDING", 1,
                "REJECTED", 2);

        return flashSaleItemRepository.findAll().stream()
                .filter(item -> slotIds.contains(item.getFlashSaleId()))
                .sorted(Comparator
                        .comparingInt((FlashSaleItem item) -> statusPriority.getOrDefault(
                                item.getStatus() != null ? item.getStatus().toUpperCase() : "", 99))
                        .thenComparing(FlashSaleItem::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(FlashSaleItem::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(FlashSaleItem::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FlashSaleItem approveRegistration(String registrationId, ApproveRegistrationRequest request) {
        FlashSaleItem registration = flashSaleItemRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found"));

        // --- APPROVAL DEADLINE VALIDATION (only blocks APPROVE action) ---
        if ("APPROVED".equals(request.getStatus())) {
            FlashSale slot = flashSaleRepository.findById(registration.getFlashSaleId()).orElse(null);
            if (slot != null) {
                FlashSaleCampaign campaign = flashSaleCampaignRepository.findById(slot.getCampaignId()).orElse(null);
                if (campaign != null && campaign.getApprovalDeadline() != null) {
                    LocalDateTime now = LocalDateTime.now();
                    if (now.isAfter(campaign.getApprovalDeadline())) {
                        throw new RuntimeException(
                                "Approval deadline has passed. You can no longer approve registrations for this campaign. Deadline was: "
                                        + campaign.getApprovalDeadline() + " (Vietnam time)");
                    }
                }
            }
        }
        // --- END VALIDATION ---

        com.shoppeclone.backend.shop.entity.Shop shop = shopService.getShopById(registration.getShopId());
        Product product = productRepository.findById(registration.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if ("APPROVED".equals(request.getStatus())) {
            // Stock was already deducted (locked) during registerProduct phase.
            // No need to deduct again here — just mark as approved.
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
            // Restore stock that was locked during registration phase
            productVariantRepository.findById(registration.getVariantId()).ifPresent(variant -> {
                variant.setStock(variant.getStock() + registration.getSaleStock());
                productVariantRepository.save(variant);
            });
            syncProductTotalStock(registration.getProductId());

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
        // Use Vietnam time — JVM timezone forced to Asia/Ho_Chi_Minh
        return flashSaleRepository.findByStartTimeAfter(LocalDateTime.now().minusDays(1));
    }

    @Override
    public Optional<FlashSale> getCurrentFlashSale() {
        // Use Vietnam time — JVM timezone forced to Asia/Ho_Chi_Minh
        LocalDateTime now = LocalDateTime.now();
        List<FlashSale> all = flashSaleRepository.findAll();
        return all.stream()
                .filter(fs -> fs.getStartTime() != null && fs.getEndTime() != null)
                .filter(fs -> "ACTIVE".equals(fs.getStatus()) || "ONGOING".equals(fs.getStatus())) // ACTIVE or ONGOING
                .filter(fs -> !fs.getStartTime().isAfter(now) && !fs.getEndTime().isBefore(now))
                .sorted(Comparator
                        .comparingInt((FlashSale fs) -> statusPriority(fs.getStatus()))
                        .thenComparing(FlashSale::getStartTime, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(FlashSale::getEndTime, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(FlashSale::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .findFirst();
    }

    private int statusPriority(String status) {
        if ("ONGOING".equalsIgnoreCase(status)) {
            return 0;
        }
        if ("ACTIVE".equalsIgnoreCase(status)) {
            return 1;
        }
        return 9;
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
        resp.setAdminNote(item.getAdminNote());
        resp.setShopId(item.getShopId());
        resp.setCreatedAt(item.getCreatedAt());
        resp.setUpdatedAt(item.getUpdatedAt());

        // Fetch Product Info
        productRepository.findById(item.getProductId()).ifPresent(p -> {
            resp.setProductName(p.getName());
            // Fetch First Image
            productImageRepository.findByProductIdOrderByDisplayOrderAsc(p.getId()).stream()
                    .findFirst()
                    .ifPresent(img -> resp.setProductImage(img.getImageUrl()));
        });

        // Fetch Variant Info (guard against null variantId)
        if (item.getVariantId() != null) {
            productVariantRepository.findById(item.getVariantId()).ifPresent(v -> {
                resp.setVariantName(
                        (v.getColor() != null ? v.getColor() : "") + " " + (v.getSize() != null ? v.getSize() : ""));
                resp.setOriginalPrice(v.getPrice());
            });
        }

        // Fetch Approval Deadline from Campaign (via Slot)
        flashSaleRepository.findById(item.getFlashSaleId()).ifPresent(slot -> {
            resp.setSlotStartTime(slot.getStartTime());
            resp.setSlotEndTime(slot.getEndTime());
            flashSaleCampaignRepository.findById(slot.getCampaignId()).ifPresent(campaign -> {
                resp.setCampaignId(campaign.getId());
                resp.setCampaignName(campaign.getName());
                resp.setApprovalDeadline(campaign.getApprovalDeadline());
            });
        });

        return resp;
    }

    @Override
    @Transactional
    public void broadcastCampaignInvitation(FlashSaleCampaign campaign) {
        log.info("[broadcastCampaignInvitation] Broadcasting invitation for campaign: '{}' (id={})", campaign.getName(),
                campaign.getId());

        List<com.shoppeclone.backend.shop.dto.response.ShopAdminResponse> activeShops = shopService.getActiveShops();
        log.info("[broadcastCampaignInvitation] Found {} active shops to notify", activeShops.size());

        if (activeShops.isEmpty()) {
            log.warn("[broadcastCampaignInvitation] No active shops found — no emails will be sent!");
            return;
        }

        String regDeadline = campaign.getRegistrationDeadline() != null ? campaign.getRegistrationDeadline().toString()
                : "Open";

        int emailsSent = 0;
        int emailsFailed = 0;
        for (com.shoppeclone.backend.shop.dto.response.ShopAdminResponse shopResp : activeShops) {
            // Send In-app Notification
            notificationService.createNotification(
                    shopResp.getOwnerId(),
                    "New Flash Sale Campaign! ⚡",
                    "Campaign '" + campaign.getName() + "' is now open for registration. Join now to boost your sales!",
                    "FLASH_SALE");

            // Send Email
            try {
                if (shopResp.getEmail() != null && !shopResp.getEmail().isBlank()) {
                    emailService.sendCampaignInvitationEmail(shopResp.getEmail(), campaign.getName(), regDeadline);
                    emailsSent++;
                } else {
                    log.warn("[broadcastCampaignInvitation] Shop '{}' has no email — skipped", shopResp.getOwnerId());
                }
            } catch (Exception e) {
                emailsFailed++;
                log.warn("Failed to send invitation email to {}: {}", shopResp.getEmail(), e.getMessage());
            }
        }
        log.info("[broadcastCampaignInvitation] Done: {} emails sent, {} failed, {} shops total",
                emailsSent, emailsFailed, activeShops.size());
    }
}
