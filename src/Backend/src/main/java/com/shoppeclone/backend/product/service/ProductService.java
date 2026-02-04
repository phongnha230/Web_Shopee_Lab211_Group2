package com.shoppeclone.backend.product.service;

import com.shoppeclone.backend.product.dto.request.CreateProductRequest;
import com.shoppeclone.backend.product.dto.request.CreateProductVariantRequest;
import com.shoppeclone.backend.product.dto.request.UpdateProductRequest;
import com.shoppeclone.backend.product.dto.response.ProductResponse;
import java.util.List;

public interface ProductService {
    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse getProductById(String id);

    List<ProductResponse> getProductsByShopId(String shopId);

    List<ProductResponse> getAllProducts();

    List<ProductResponse> getProductsByCategory(String categoryId);

    ProductResponse updateProduct(String id, UpdateProductRequest request);

    void deleteProduct(String id);

    // Product variant management
    void addVariant(String productId, CreateProductVariantRequest request);

    void removeVariant(String variantId);

    // Category management
    void addCategory(String productId, String categoryId);

    void removeCategory(String productId, String categoryId);

    // Image management
    void addImage(String productId, String imageUrl);

    void removeImage(String imageId);
}
