package com.shoppeclone.backend.product.service.impl;

import com.shoppeclone.backend.product.dto.request.CreateProductRequest;
import com.shoppeclone.backend.product.dto.request.CreateProductVariantRequest;
import com.shoppeclone.backend.product.dto.request.UpdateProductRequest;
import com.shoppeclone.backend.product.dto.response.ProductResponse;
import com.shoppeclone.backend.product.dto.response.ProductVariantResponse;
import com.shoppeclone.backend.product.entity.*;
import com.shoppeclone.backend.product.repository.*;
import com.shoppeclone.backend.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository imageRepository;
    private final ProductCategoryRepository productCategoryRepository;

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        Product product = new Product();
        product.setShopId(request.getShopId());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);
        boolean hasVariants = false;

        // 1. Save Variants
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            hasVariants = true;
            for (CreateProductVariantRequest vReq : request.getVariants()) {
                ProductVariant variant = new ProductVariant();
                variant.setProductId(savedProduct.getId());
                variant.setSize(vReq.getSize());
                variant.setColor(vReq.getColor());
                variant.setPrice(vReq.getPrice());
                variant.setStock(vReq.getStock());
                variant.setImageUrl(vReq.getImageUrl());
                variant.setCreatedAt(LocalDateTime.now());
                variant.setUpdatedAt(LocalDateTime.now());
                variantRepository.save(variant);
            }

            // Calculate Min/Max Price and Total Stock for Product Display
            List<CreateProductVariantRequest> variantList = request.getVariants();
            BigDecimal minPrice = variantList.stream()
                    .map(CreateProductVariantRequest::getPrice)
                    .min(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);
            BigDecimal maxPrice = variantList.stream()
                    .map(CreateProductVariantRequest::getPrice)
                    .max(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);
            Integer totalStock = variantList.stream()
                    .mapToInt(CreateProductVariantRequest::getStock)
                    .sum();

            savedProduct.setMinPrice(minPrice);
            savedProduct.setMaxPrice(maxPrice);
            savedProduct.setTotalStock(totalStock);
            productRepository.save(savedProduct);
        }

        // 2. Save Images
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            int order = 0;
            for (String url : request.getImages()) {
                ProductImage image = new ProductImage();
                image.setProductId(savedProduct.getId());
                image.setImageUrl(url);
                image.setDisplayOrder(order++);
                image.setCreatedAt(LocalDateTime.now());
                imageRepository.save(image);
            }
        }

        // 3. Save Category
        if (request.getCategoryId() != null && !request.getCategoryId().isEmpty()) {
            ProductCategory productCategory = new ProductCategory();
            productCategory.setProductId(savedProduct.getId());
            productCategory.setCategoryId(request.getCategoryId());
            productCategoryRepository.save(productCategory);
        }

        return mapToResponse(savedProduct);
    }

    @Override
    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return mapToResponse(product);
    }

    @Override
    public List<ProductResponse> getProductsByShopId(String shopId) {
        return productRepository.findByShopId(shopId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse updateProduct(String id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }
        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);
        return mapToResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(String id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found");
        }

        // Delete related data
        variantRepository.deleteByProductId(id);
        imageRepository.deleteByProductId(id);
        productCategoryRepository.deleteByProductId(id);

        productRepository.deleteById(id);
    }

    @Override
    public void addVariant(String productId, CreateProductVariantRequest request) {
        if (!productRepository.existsById(productId)) {
            throw new RuntimeException("Product not found");
        }

        ProductVariant variant = new ProductVariant();
        variant.setProductId(productId);
        variant.setSize(request.getSize());
        variant.setColor(request.getColor());
        variant.setPrice(request.getPrice());
        variant.setStock(request.getStock());
        variant.setImageUrl(request.getImageUrl());
        variant.setCreatedAt(LocalDateTime.now());
        variant.setUpdatedAt(LocalDateTime.now());

        variantRepository.save(variant);
    }

    @Override
    public void removeVariant(String variantId) {
        if (!variantRepository.existsById(variantId)) {
            throw new RuntimeException("Variant not found");
        }
        variantRepository.deleteById(variantId);
    }

    @Override
    public void addCategory(String productId, String categoryId) {
        if (!productRepository.existsById(productId)) {
            throw new RuntimeException("Product not found");
        }

        ProductCategory productCategory = new ProductCategory();
        productCategory.setProductId(productId);
        productCategory.setCategoryId(categoryId);

        productCategoryRepository.save(productCategory);
    }

    @Override
    public void removeCategory(String productId, String categoryId) {
        productCategoryRepository.deleteByProductIdAndCategoryId(productId, categoryId);
    }

    @Override
    public void addImage(String productId, String imageUrl) {
        if (!productRepository.existsById(productId)) {
            throw new RuntimeException("Product not found");
        }

        ProductImage image = new ProductImage();
        image.setProductId(productId);
        image.setImageUrl(imageUrl);
        image.setCreatedAt(LocalDateTime.now());

        imageRepository.save(image);
    }

    @Override
    public void removeImage(String imageId) {
        if (!imageRepository.existsById(imageId)) {
            throw new RuntimeException("Image not found");
        }
        imageRepository.deleteById(imageId);
    }

    private ProductResponse mapToResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setShopId(product.getShopId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setStatus(product.getStatus());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        // Load variants
        List<ProductVariantResponse> variants = variantRepository.findByProductId(product.getId()).stream()
                .map(this::mapVariantToResponse)
                .collect(Collectors.toList());
        response.setVariants(variants);

        // Load images
        List<String> images = imageRepository.findByProductIdOrderByDisplayOrderAsc(product.getId()).stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());
        response.setImages(images);

        // Load categories
        List<String> categories = productCategoryRepository.findByProductId(product.getId()).stream()
                .map(ProductCategory::getCategoryId)
                .collect(Collectors.toList());
        response.setCategories(categories);

        return response;
    }

    private ProductVariantResponse mapVariantToResponse(ProductVariant variant) {
        ProductVariantResponse response = new ProductVariantResponse();
        response.setId(variant.getId());
        response.setProductId(variant.getProductId());
        response.setSize(variant.getSize());
        response.setColor(variant.getColor());
        response.setPrice(variant.getPrice());
        response.setStock(variant.getStock());
        response.setImageUrl(variant.getImageUrl());
        response.setCreatedAt(variant.getCreatedAt());
        response.setUpdatedAt(variant.getUpdatedAt());
        return response;
    }
}
