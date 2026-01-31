package com.shoppeclone.backend.shop.service.impl;

import com.shoppeclone.backend.auth.model.Role;
import com.shoppeclone.backend.auth.model.User;
import com.shoppeclone.backend.auth.repository.RoleRepository;
import com.shoppeclone.backend.auth.repository.UserRepository;
import com.shoppeclone.backend.shop.dto.ShopRegisterRequest;
import com.shoppeclone.backend.shop.dto.UpdateShopRequest;
import com.shoppeclone.backend.shop.entity.Shop;
import com.shoppeclone.backend.shop.entity.ShopStatus;
import com.shoppeclone.backend.shop.repository.ShopRepository;
import com.shoppeclone.backend.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final com.shoppeclone.backend.user.service.NotificationService notificationService;

    @Override
    @Transactional
    public Shop registerShop(String userEmail, ShopRegisterRequest request) {
        User user = getUserByEmail(userEmail);

        // Check if user already has a shop
        if (shopRepository.existsByOwnerId(user.getId())) {
            throw new RuntimeException("You already have a shop registered!");
        }

        Shop shop = new Shop();
        shop.setOwnerId(user.getId());
        shop.setName(request.getName());
        shop.setAddress(request.getAddress());
        shop.setPhone(request.getPhone());
        shop.setEmail(request.getEmail());
        shop.setDescription(request.getDescription());

        // ✅ Map ID Card Images
        shop.setIdCardFront(request.getIdCardFront());
        shop.setIdCardBack(request.getIdCardBack());

        // Map Step 2 Info
        shop.setIdentityIdCard(request.getIdentityIdCard());
        shop.setBankName(request.getBankName());
        shop.setBankAccountNumber(request.getBankAccountNumber());
        shop.setBankAccountHolder(request.getBankAccountHolder());

        shop.setStatus(ShopStatus.PENDING);
        shop.setCreatedAt(LocalDateTime.now());
        shop.setUpdatedAt(LocalDateTime.now());

        return shopRepository.save(shop);
    }

    @Override
    public Shop getMyShop(String userEmail) {
        User user = getUserByEmail(userEmail);
        return shopRepository.findByOwnerId(user.getId()).orElse(null);
    }

    @Override
    public List<Shop> getPendingShops() {
        return shopRepository.findByStatus(ShopStatus.PENDING);
    }

    @Override
    @Transactional
    public void approveShop(String shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        if (shop.getStatus() == ShopStatus.ACTIVE) {
            return; // Already approved
        }

        // 1. Update Shop Status
        shop.setStatus(ShopStatus.ACTIVE);
        shop.setUpdatedAt(LocalDateTime.now());
        shopRepository.save(shop);

        // 2. Promote User to SELLER
        User user = userRepository.findById(shop.getOwnerId())
                .orElseThrow(() -> new RuntimeException("Shop owner not found"));

        Role sellerRole = roleRepository.findByName("ROLE_SELLER")
                .orElseThrow(() -> new RuntimeException("Role ROLE_SELLER not found"));

        boolean hasRole = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_SELLER"));
        if (!hasRole) {
            user.getRoles().add(sellerRole);
            userRepository.save(user);
        }

        // 3. Send Notification
        notificationService.createNotification(
                user.getId(),
                "Shop Approved 🎉",
                "Congratulations! Your shop '" + shop.getName() + "' has been approved. You can now start selling.",
                "SHOP_APPROVED");
    }

    @Override
    @Transactional
    public void rejectShop(String shopId, String reason) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        shop.setStatus(ShopStatus.REJECTED);
        shop.setRejectionReason(reason);
        shop.setUpdatedAt(LocalDateTime.now());
        shopRepository.save(shop);

        // Send Notification
        notificationService.createNotification(
                shop.getOwnerId(),
                "Shop Registration Rejected ❌",
                "Your shop application for '" + shop.getName() + "' was rejected. Reason: " + reason,
                "SHOP_REJECTED");
    }

    @Override
    @Transactional
    public Shop updateShop(String userEmail, UpdateShopRequest request) {
        Shop shop = getMyShop(userEmail);
        if (shop == null) {
            throw new RuntimeException("Shop not found for user: " + userEmail);
        }

        if (request.getName() != null)
            shop.setName(request.getName());
        if (request.getDescription() != null)
            shop.setDescription(request.getDescription());
        if (request.getAddress() != null)
            shop.setAddress(request.getAddress());
        if (request.getPhone() != null)
            shop.setPhone(request.getPhone());
        if (request.getEmail() != null)
            shop.setEmail(request.getEmail());
        if (request.getLogo() != null)
            shop.setLogo(request.getLogo());

        shop.setUpdatedAt(LocalDateTime.now());
        return shopRepository.save(shop);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
