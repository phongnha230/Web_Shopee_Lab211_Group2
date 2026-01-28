package com.shoppeclone.backend.shop.controller;

import com.shoppeclone.backend.common.service.CloudinaryService;
import com.shoppeclone.backend.shop.dto.ShopRegisterRequest;
import com.shoppeclone.backend.shop.entity.Shop;
import com.shoppeclone.backend.shop.service.ShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ShopController {

    private final ShopService shopService;
    private final CloudinaryService cloudinaryService;

    private String getEmail(Authentication authentication) {
        return authentication.getName();
    }

    /**
     * Upload ID card image (front or back)
     * Returns Cloudinary URL to be used in shop registration
     */
    @PostMapping("/upload-id-card")
    public ResponseEntity<?> uploadIdCardImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type) {
        try {
            // Validate type parameter
            if (!type.equals("front") && !type.equals("back")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid type. Must be 'front' or 'back'");
                return ResponseEntity.badRequest().body(error);
            }

            // Upload to Cloudinary
            String folder = "shop_id_cards/" + type;
            String imageUrl = cloudinaryService.uploadImage(file, folder);

            // Return URL
            Map<String, String> response = new HashMap<>();
            response.put("url", imageUrl);
            response.put("type", type);
            response.put("message", "Image uploaded successfully");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);

        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Shop> registerShop(
            @Valid @RequestBody ShopRegisterRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(shopService.registerShop(getEmail(authentication), request));
    }

    @GetMapping("/my-shop")
    public ResponseEntity<Shop> getMyShop(Authentication authentication) {
        return ResponseEntity.ok(shopService.getMyShop(getEmail(authentication)));
    }

    // ADMIN ONLY
    @GetMapping("/admin/pending")
    // @PreAuthorize("hasRole('ADMIN')") // Uncomment if using Method Security
    public ResponseEntity<List<Shop>> getPendingShops() {
        return ResponseEntity.ok(shopService.getPendingShops());
    }

    @PostMapping("/admin/approve/{shopId}")
    public ResponseEntity<String> approveShop(@PathVariable String shopId) {
        shopService.approveShop(shopId);
        return ResponseEntity.ok("Shop approved and User promoted to Seller!");
    }

    @PostMapping("/admin/reject/{shopId}")
    public ResponseEntity<String> rejectShop(@PathVariable String shopId,
            @RequestParam(required = false) String reason) {
        shopService.rejectShop(shopId, reason);
        return ResponseEntity.ok("Shop rejected.");
    }
}
