package com.shoppeclone.backend.search.service;

import com.shoppeclone.backend.product.entity.Product;
import com.shoppeclone.backend.product.entity.ProductCategory;
import com.shoppeclone.backend.product.repository.ProductCategoryRepository;
import com.shoppeclone.backend.product.repository.ProductImageRepository;
import com.shoppeclone.backend.product.repository.ProductRepository;
import com.shoppeclone.backend.search.entity.ProductDocument;
import com.shoppeclone.backend.search.repository.ElasticsearchProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchDataSyncService {

    private final ProductRepository productRepository;
    private final ElasticsearchProductRepository elasticsearchProductRepository;
    private final ProductImageRepository imageRepository;
    private final ProductCategoryRepository productCategoryRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void syncDataToElasticsearch() {
        log.info("Bắt đầu đồng bộ dữ liệu từ MongoDB sang Elasticsearch...");
        
        // Cần xoá toàn bộ index cũ nếu muốn đồng bộ sạch (tuỳ chọn)
        elasticsearchProductRepository.deleteAll();

        List<Product> products = productRepository.findAll();
        
        List<ProductDocument> documents = products.stream().map(this::mapToDocument).collect(Collectors.toList());
        
        elasticsearchProductRepository.saveAll(documents);
        
        log.info("Đã đồng bộ {} sản phẩm sang Elasticsearch thành công!", documents.size());
    }

    private ProductDocument mapToDocument(Product product) {
        ProductDocument doc = ProductDocument.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .shopId(product.getShopId())
                .status(product.getStatus() != null ? product.getStatus().name() : null)
                .minPrice(product.getMinPrice())
                .maxPrice(product.getMaxPrice())
                .sold(product.getSold())
                .isFlashSale(product.getIsFlashSale())
                .build();
        
        // Hình ảnh đại diện
        imageRepository.findByProductIdOrderByDisplayOrderAsc(product.getId()).stream()
                .findFirst()
                .ifPresent(img -> doc.setImageUrl(img.getImageUrl()));

        // Danh mục
        List<String> categories = productCategoryRepository.findByProductId(product.getId()).stream()
                .map(ProductCategory::getCategoryId)
                .collect(Collectors.toList());
        doc.setCategories(categories);

        return doc;
    }
}
