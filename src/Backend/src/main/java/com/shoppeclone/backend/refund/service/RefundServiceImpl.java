package com.shoppeclone.backend.refund.service;

import com.shoppeclone.backend.order.entity.Order;
import com.shoppeclone.backend.order.entity.OrderItem;
import com.shoppeclone.backend.order.entity.OrderStatus;
import com.shoppeclone.backend.order.repository.OrderRepository;
import com.shoppeclone.backend.product.entity.Product;
import com.shoppeclone.backend.product.entity.ProductVariant;
import com.shoppeclone.backend.product.repository.ProductRepository;
import com.shoppeclone.backend.product.repository.ProductVariantRepository;
import com.shoppeclone.backend.promotion.flashsale.repository.FlashSaleItemRepository;
import com.shoppeclone.backend.promotion.flashsale.repository.FlashSaleRepository;
import com.shoppeclone.backend.refund.entity.Refund;
import com.shoppeclone.backend.refund.entity.RefundStatus;
import com.shoppeclone.backend.refund.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final RefundRepository refundRepository;
    private final OrderRepository orderRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final FlashSaleRepository flashSaleRepository;

    @Override
    public Refund createRefund(String orderId, String buyerId, BigDecimal amount, String reason) {
        // Check if refund already exists for this order
        if (refundRepository.findByOrderId(orderId).isPresent()) {
            throw new RuntimeException("Refund already requested for this order");
        }

        Refund refund = new Refund();
        refund.setOrderId(orderId);
        refund.setBuyerId(buyerId);
        refund.setAmount(amount);
        refund.setReason(reason);
        refund.setStatus(RefundStatus.REQUESTED);

        return refundRepository.save(refund);
    }

    @Override
    public Refund createAndApproveRefund(String orderId, String buyerId, BigDecimal amount, String reason,
            String adminId) {

        Optional<Refund> existingRefundOpt = refundRepository.findByOrderId(orderId);
        Refund refund;

        if (existingRefundOpt.isPresent()) {
            refund = existingRefundOpt.get();
            // If already approved, maybe we shouldn't restore stock again, but assume it's
            // safe to update status
            if (refund.getStatus() == RefundStatus.APPROVED || refund.getStatus() == RefundStatus.REFUNDED) {
                return refund; // Already handled
            }
        } else {
            refund = new Refund();
            refund.setOrderId(orderId);
            refund.setBuyerId(buyerId);
        }

        refund.setAmount(amount);
        refund.setReason(reason);
        refund.setStatus(RefundStatus.APPROVED);
        refund.setApprovedBy(adminId);
        refund.setProcessedAt(LocalDateTime.now());

        // Restore stock & mark order as RETURNED
        restoreStockAndMarkReturned(orderId);

        return refundRepository.save(refund);
    }

    @Override
    public Refund approveRefund(String refundId, String adminId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Refund not found"));

        if (refund.getStatus() != RefundStatus.REQUESTED) {
            throw new RuntimeException("Refund status must be REQUESTED to approve");
        }

        refund.setStatus(RefundStatus.APPROVED);
        refund.setApprovedBy(adminId);
        refund.setProcessedAt(LocalDateTime.now());

        // 1. Restore stock for all items in the order
        // 2. Mark order status as RETURNED
        restoreStockAndMarkReturned(refund.getOrderId());

        return refundRepository.save(refund);
    }

    @Override
    public Refund rejectRefund(String refundId, String adminId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Refund not found"));

        if (refund.getStatus() != RefundStatus.REQUESTED) {
            throw new RuntimeException("Refund status must be REQUESTED to reject");
        }

        refund.setStatus(RefundStatus.REJECTED);
        refund.setApprovedBy(adminId);
        refund.setProcessedAt(LocalDateTime.now());

        return refundRepository.save(refund);
    }

    @Override
    public List<Refund> getAllRefunds() {
        return refundRepository.findAll();
    }

    @Override
    public Refund getRefundByOrderId(String orderId) {
        return refundRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Refund not found for orderId: " + orderId));
    }

    @Override
    public Optional<Refund> findOptionalByOrderId(String orderId) {
        return refundRepository.findByOrderId(orderId);
    }

    // ─── Private Helpers ────────────────────────────────────────────────────────

    /**
     * Hoàn lại stock cho tất cả items trong đơn hàng và đổi trạng thái đơn
     * sang RETURNED. Review của người dùng KHÔNG bị xóa — đây là hành vi đúng,
     * tương tự Shopee thực tế.
     */
    private void restoreStockAndMarkReturned(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        for (OrderItem item : order.getItems()) {
            ProductVariant variant = productVariantRepository.findById(item.getVariantId()).orElse(null);
            if (variant == null)
                continue;

            // Hoàn stock chính
            variant.setStock(variant.getStock() + item.getQuantity());

            // Hoàn Flash Sale stock nếu có
            restoreFlashSaleItemStock(variant.getProductId(), variant.getId(), item.getQuantity());

            productVariantRepository.save(variant);

            // Giảm product.sold nếu đơn đã từng COMPLETED
            if (order.getOrderStatus() == OrderStatus.COMPLETED) {
                Product product = productRepository.findById(variant.getProductId()).orElse(null);
                if (product != null) {
                    int currentSold = product.getSold() != null ? product.getSold() : 0;
                    product.setSold(Math.max(0, currentSold - item.getQuantity()));
                    product.setUpdatedAt(LocalDateTime.now());
                    productRepository.save(product);
                }
            }
        }

        // Đổi trạng thái đơn hàng sang RETURNED
        order.setOrderStatus(OrderStatus.RETURNED);
        order.setReturnedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    /**
     * Hoàn lại Flash Sale remaining stock (giống logic trong
     * OrderServiceImpl.restoreFlashSaleItemStock)
     */
    private void restoreFlashSaleItemStock(String productId, String variantId, int quantity) {
        List<com.shoppeclone.backend.promotion.flashsale.entity.FlashSaleItem> items = flashSaleItemRepository
                .findByProductIdAndStatus(productId, "APPROVED");

        for (com.shoppeclone.backend.promotion.flashsale.entity.FlashSaleItem item : items) {
            com.shoppeclone.backend.promotion.flashsale.entity.FlashSale slot = flashSaleRepository
                    .findById(item.getFlashSaleId()).orElse(null);

            if (slot != null && "ONGOING".equals(slot.getStatus())) {
                if (item.getVariantId() == null || item.getVariantId().equals(variantId)) {
                    item.setRemainingStock(
                            (item.getRemainingStock() != null ? item.getRemainingStock() : 0) + quantity);
                    flashSaleItemRepository.save(item);

                    // Giảm flashSaleSold của variant
                    ProductVariant variant = productVariantRepository.findById(variantId).orElse(null);
                    if (variant != null) {
                        int fsSold = variant.getFlashSaleSold() != null ? variant.getFlashSaleSold() : 0;
                        variant.setFlashSaleSold(Math.max(0, fsSold - quantity));
                        productVariantRepository.save(variant);

                        Product product = productRepository.findById(productId).orElse(null);
                        if (product != null) {
                            int prodFsSold = product.getFlashSaleSold() != null ? product.getFlashSaleSold() : 0;
                            product.setFlashSaleSold(Math.max(0, prodFsSold - quantity));
                            productRepository.save(product);
                        }
                    }
                    break;
                }
            }
        }
    }
}
