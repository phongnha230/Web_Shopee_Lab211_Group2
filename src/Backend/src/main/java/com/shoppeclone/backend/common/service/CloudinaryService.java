package com.shoppeclone.backend.common.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Upload image to Cloudinary and return secure URL
     * If Cloudinary fails, fallback to local storage
     *
     * @param file   MultipartFile to upload
     * @param folder Target folder in Cloudinary (e.g., "shop_id_cards")
     * @return Secure HTTPS URL of uploaded image or Local URL
     * @throws IOException if upload fails
     */
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        // Validate file
        validateImage(file);

        try {
            // Upload to Cloudinary
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "image");

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("Image uploaded successfully to Cloudinary: {}", secureUrl);

            return secureUrl;
        } catch (Exception e) {
            log.warn(
                    "Cloudinary upload failed (likely due to missing config). Falling back to local storage. Error: {}",
                    e.getMessage());
            return saveFileLocally(file);
        }
    }

    private String saveFileLocally(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        // Define paths
        Path uploadDir = Paths.get("src", "main", "resources", "static", "uploads");
        Path targetDir = Paths.get("target", "classes", "static", "uploads");

        // Ensure directories exist
        if (!Files.exists(uploadDir))
            Files.createDirectories(uploadDir);
        if (!Files.exists(targetDir))
            Files.createDirectories(targetDir);

        // Save to target (for immediate serving)
        try (InputStream inputStream = file.getInputStream()) {
            Path filePath = targetDir.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        // Save to src (for persistence)
        try (InputStream inputStream = file.getInputStream()) {
            Path filePath = uploadDir.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        // Construct URL
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        return baseUrl + "/uploads/" + fileName;
    }

    /**
     * Validate image file
     */
    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Check file size (max 5MB for ID cards)
        long maxSize = 5 * 1024 * 1024; // 5MB in bytes
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 5MB");
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Invalid file type. Only images are allowed.");
        }

        // Check specific image formats
        String[] allowedTypes = { "image/jpeg", "image/jpg", "image/png", "image/webp" };
        boolean isValidType = false;
        for (String allowedType : allowedTypes) {
            if (contentType.equalsIgnoreCase(allowedType)) {
                isValidType = true;
                break;
            }
        }

        if (!isValidType) {
            throw new IllegalArgumentException("Unsupported image format. Allowed: JPEG, JPG, PNG, WEBP");
        }
    }

    /**
     * Delete image from Cloudinary by public ID
     */
    public void deleteImage(String publicId) throws IOException {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Image deleted from Cloudinary: {}", publicId);
        } catch (Exception e) {
            log.warn("Failed to delete image from Cloudinary (local file?): {}", e.getMessage());
        }
    }
}
