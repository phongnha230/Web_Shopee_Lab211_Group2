package com.shoppeclone.backend.shop.service.impl;

import com.shoppeclone.backend.shop.dto.request.CreateShopRequest;
import com.shoppeclone.backend.shop.dto.request.UpdateShopRequest;
import com.shoppeclone.backend.shop.dto.response.ShopResponse;
import com.shoppeclone.backend.shop.entity.Shop;
import com.shoppeclone.backend.shop.repository.ShopRepository;
import com.shoppeclone.backend.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;

    @Override
    public ShopResponse createShop(CreateShopRequest request) {
        // Check if seller already has a shop
        if (shopRepository.existsBySellerId(request.getSellerId())) {
            throw new RuntimeException("Seller already has a shop");
        }

        Shop shop = new Shop();
        shop.setSellerId(request.getSellerId());
        shop.setName(request.getName());

        // Set pickup address
        shop.setProvince(request.getProvince());
        shop.setDistrict(request.getDistrict());
        shop.setWard(request.getWard());
        shop.setAddress(request.getAddress());

        // Set contact information
        shop.setPhone(request.getPhone());
        shop.setEmail(request.getEmail());

        shop.setCreatedAt(LocalDateTime.now());
        shop.setUpdatedAt(LocalDateTime.now());

        Shop savedShop = shopRepository.save(shop);
        return mapToResponse(savedShop);
    }

    @Override
    public ShopResponse getShopById(String id) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        return mapToResponse(shop);
    }

    @Override
    public ShopResponse getShopBySellerId(String sellerId) {
        Shop shop = shopRepository.findBySellerId(sellerId)
                .orElseThrow(() -> new RuntimeException("Shop not found for this seller"));
        return mapToResponse(shop);
    }

    @Override
    public List<ShopResponse> getAllShops() {
        return shopRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ShopResponse updateShop(String id, UpdateShopRequest request) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        if (request.getName() != null) {
            shop.setName(request.getName());
        }

        // Update address if provided
        if (request.getProvince() != null) {
            shop.setProvince(request.getProvince());
        }
        if (request.getDistrict() != null) {
            shop.setDistrict(request.getDistrict());
        }
        if (request.getWard() != null) {
            shop.setWard(request.getWard());
        }
        if (request.getAddress() != null) {
            shop.setAddress(request.getAddress());
        }

        // Update contact if provided
        if (request.getPhone() != null) {
            shop.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            shop.setEmail(request.getEmail());
        }

        if (request.getStatus() != null) {
            shop.setStatus(request.getStatus());
        }
        shop.setUpdatedAt(LocalDateTime.now());

        Shop updatedShop = shopRepository.save(shop);
        return mapToResponse(updatedShop);
    }

    @Override
    public void deleteShop(String id) {
        if (!shopRepository.existsById(id)) {
            throw new RuntimeException("Shop not found");
        }
        shopRepository.deleteById(id);
    }

    private ShopResponse mapToResponse(Shop shop) {
        ShopResponse response = new ShopResponse();
        response.setId(shop.getId());
        response.setSellerId(shop.getSellerId());
        response.setName(shop.getName());
        response.setRating(shop.getRating());

        // Map pickup address (for Shipper integration)
        response.setProvince(shop.getProvince());
        response.setDistrict(shop.getDistrict());
        response.setWard(shop.getWard());
        response.setAddress(shop.getAddress());

        // Map contact information
        response.setPhone(shop.getPhone());
        response.setEmail(shop.getEmail());

        response.setStatus(shop.getStatus());
        response.setCreatedAt(shop.getCreatedAt());
        response.setUpdatedAt(shop.getUpdatedAt());
        return response;
    }
}
