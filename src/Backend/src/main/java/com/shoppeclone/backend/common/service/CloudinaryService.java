package com.shoppeclone.backend.common.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Upload image to Cloudinary and return secure URL
     * 
     * @param file   MultipartFile to upload
     * @param folder Target folder in Cloudinary (e.g., "shop_id_cards")
     * @return Secure HTTPS URL of uploaded image
     * @throws IOException if upload fails
     */
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        // Validate file
        validateImage(file);

        // Upload to Cloudinary
        Map<String, Object> uploadParams = ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "image");

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

        String secureUrl = (String) uploadResult.get("secure_url");
        log.info("Image uploaded successfully to Cloudinary: {}", secureUrl);

        return secureUrl;
    }

    /**
     * Validate image file
     */
    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Check file size (max 3MB for review images - e-commerce standard)
        long maxSize = 3 * 1024 * 1024; // 3MB in bytes
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 3MB");
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
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        log.info("Image deleted from Cloudinary: {}", publicId);
    }
}
