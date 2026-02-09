package com.shoppeclone.backend.review.sync;

import com.shoppeclone.backend.order.entity.OrderItem;
import com.shoppeclone.backend.order.entity.OrderStatus;
import com.shoppeclone.backend.order.repository.OrderRepository;
import com.shoppeclone.backend.product.entity.Product;
import com.shoppeclone.backend.product.repository.ProductRepository;
import com.shoppeclone.backend.product.repository.ProductVariantRepository;
import com.shoppeclone.backend.review.entity.Review;
import com.shoppeclone.backend.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Syncs product.rating, product.reviewCount from reviews,
 * and product.sold from completed orders.
 */
@Component
@Order(5)
@RequiredArgsConstructor
@Slf4j
public class ProductReviewSyncRunner implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;

    @Override
    public void run(String... args) {
        log.info("🔄 Syncing product rating, reviewCount, and sold...");
        syncReviewData();
        syncSoldData();
        log.info("✅ Product review/sold sync completed.");
    }

    private void syncReviewData() {
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            List<Review> reviews = reviewRepository.findByProductId(product.getId());
            if (reviews.isEmpty()) {
                product.setRating(0.0);
                product.setReviewCount(0);
            } else {
                double avg = reviews.stream().mapToInt(Review::getRating).sum() / (double) reviews.size();
                product.setRating(Math.round(avg * 10.0) / 10.0);
                product.setReviewCount(reviews.size());
            }
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);
        }
    }

    private void syncSoldData() {
        List<com.shoppeclone.backend.order.entity.Order> completedOrders = orderRepository.findAll().stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.COMPLETED)
                .collect(Collectors.toList());

        Map<String, Integer> productSoldMap = completedOrders.stream()
                .flatMap(o -> o.getItems().stream())
                .collect(Collectors.groupingBy(
                        item -> {
                            var v = productVariantRepository.findById(item.getVariantId()).orElse(null);
                            return v != null ? v.getProductId() : "_unknown_";
                        },
                        Collectors.summingInt(OrderItem::getQuantity)
                ));

        productSoldMap.remove("_unknown_");

        for (Map.Entry<String, Integer> e : productSoldMap.entrySet()) {
            productRepository.findById(e.getKey()).ifPresent(p -> {
                p.setSold(e.getValue());
                p.setUpdatedAt(LocalDateTime.now());
                productRepository.save(p);
            });
        }
    }
}
