package com.shoppeclone.backend.dispute.service.impl;

import com.shoppeclone.backend.dispute.entity.Dispute;
import com.shoppeclone.backend.dispute.entity.DisputeStatus;
import com.shoppeclone.backend.dispute.repository.DisputeRepository;
import com.shoppeclone.backend.dispute.service.DisputeService;
import com.shoppeclone.backend.order.entity.Order;
import com.shoppeclone.backend.order.entity.OrderItem;
import com.shoppeclone.backend.product.entity.Product;
import com.shoppeclone.backend.product.entity.ProductVariant;
import com.shoppeclone.backend.product.repository.ProductRepository;
import com.shoppeclone.backend.product.repository.ProductVariantRepository;
import com.shoppeclone.backend.refund.service.RefundService;
import com.shoppeclone.backend.review.repository.ReviewRepository;
import com.shoppeclone.backend.order.entity.OrderStatus;
import com.shoppeclone.backend.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DisputeServiceImpl implements DisputeService {

    private final DisputeRepository disputeRepository;
    private final OrderRepository orderRepository;
    private final RefundService refundService;
    private final ReviewRepository reviewRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final com.shoppeclone.backend.dispute.repository.DisputeImageRepository disputeImageRepository;

    @Override
    public com.shoppeclone.backend.dispute.entity.DisputeImage addDisputeImage(String disputeId, String imageUrl,
            String uploadedBy) {
        // Verify dispute exists but logic is simple for now
        getDispute(disputeId);

        com.shoppeclone.backend.dispute.entity.DisputeImage image = new com.shoppeclone.backend.dispute.entity.DisputeImage();
        image.setDisputeId(disputeId);
        image.setImageUrl(imageUrl);
        image.setUploadedBy(uploadedBy);

        return disputeImageRepository.save(image);
    }

    @Override
    public List<com.shoppeclone.backend.dispute.entity.DisputeImage> getDisputeImages(String disputeId) {
        return disputeImageRepository.findByDisputeId(disputeId);
    }

    @Override
    @Transactional
    public Dispute createDispute(String orderId, String buyerId, String reason, String description) {
        // 1. Check if dispute already exists
        if (disputeRepository.findByOrderId(orderId).isPresent()) {
            throw new RuntimeException("Dispute already exists for this order");
        }

        // 2. Validate Order eligibility (e.g., must be SHIPPED or DELIVERED/COMPLETED
        // within X days)
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUserId().equals(buyerId)) {
            throw new RuntimeException("Unauthorized: Buyer does not own this order");
        }

        // Simplistic check: Allow dispute if Shipped or Delivered.
        // In real world, maybe also allow if Cancelled but issues arise, but per spec:
        // "Status: SHIPPED | COMPLETED (in X days)"
        // 5. Rule: Strictly allow Dispute only for SHIPPED or COMPLETED (within 7 days)
        boolean isEligible = false;

        if (order.getOrderStatus() == OrderStatus.SHIPPED) {
            isEligible = true;
        } else if (order.getOrderStatus() == OrderStatus.COMPLETED) {
            if (order.getCompletedAt() == null) {
                // Should not happen if data is consistent, but safeguard
                throw new RuntimeException("Order is COMPLETED but has no completedAt date");
            }
            LocalDateTime deadline = order.getCompletedAt().plusDays(7);
            if (LocalDateTime.now().isBefore(deadline) || LocalDateTime.now().isEqual(deadline)) {
                isEligible = true;
            }
        }

        if (!isEligible) {
            throw new RuntimeException("Dispute not allowed. Order must be SHIPPED or COMPLETED within 7 days.");
        }

        // 3. Block if buyer has already reviewed any product in this order
        List<com.shoppeclone.backend.review.entity.Review> existingReviews = reviewRepository.findByUserIdAndOrderId(buyerId, orderId);
        if (!existingReviews.isEmpty()) {
            throw new RuntimeException("Bạn đã đánh giá sản phẩm trong đơn này, không thể khiếu nại.");
        }

        // 4. Create Dispute
        Dispute dispute = new Dispute();
        dispute.setOrderId(orderId);
        dispute.setBuyerId(buyerId);
        // dispute.setSellerId(order.getSellerId()); // Assuming Order has sellerId, if
        // multi-vendor. For now omitted or null if not applicable.
        dispute.setReason(reason);
        dispute.setDescription(description);
        dispute.setStatus(DisputeStatus.OPEN);

        return disputeRepository.save(dispute);
    }

    @Override
    public Dispute updateDisputeStatus(String disputeId, String statusStr, String adminNote) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new RuntimeException("Dispute not found"));

        try {
            DisputeStatus newStatus = DisputeStatus.valueOf(statusStr.toUpperCase());
            dispute.setStatus(newStatus);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + statusStr);
        }

        if (adminNote != null) {
            dispute.setAdminNote(adminNote);
        }

        if (dispute.getStatus() == DisputeStatus.RESOLVED || dispute.getStatus() == DisputeStatus.REJECTED) {
            dispute.setResolvedAt(LocalDateTime.now());
        }

        return disputeRepository.save(dispute);
    }

    @Override
    @Transactional
    public Dispute updateDisputeStatusWithRefund(String disputeId, String statusStr, String adminNote,
            boolean approveRefund, BigDecimal refundAmount, String adminId) {
        Dispute dispute = updateDisputeStatus(disputeId, statusStr, adminNote);

        if (dispute.getStatus() == DisputeStatus.RESOLVED && approveRefund) {
            Order order = orderRepository.findById(dispute.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            BigDecimal amount = refundAmount != null && refundAmount.compareTo(BigDecimal.ZERO) > 0
                    ? refundAmount
                    : order.getTotalPrice();
            if (amount == null) amount = BigDecimal.ZERO;
            String reason = "Từ khiếu nại: " + dispute.getReason();
            refundService.createAndApproveRefund(dispute.getOrderId(), dispute.getBuyerId(), amount, reason, adminId);

            // Hồi hàng về kho seller (restore stock, giảm sold)
            for (OrderItem item : order.getItems()) {
                ProductVariant variant = productVariantRepository.findById(item.getVariantId()).orElse(null);
                if (variant != null) {
                    variant.setStock((variant.getStock() != null ? variant.getStock() : 0) + item.getQuantity());
                    productVariantRepository.save(variant);
                    Product product = productRepository.findById(variant.getProductId()).orElse(null);
                    if (product != null && product.getSold() != null && product.getSold() > 0) {
                        product.setSold(Math.max(0, product.getSold() - item.getQuantity()));
                        productRepository.save(product);
                    }
                }
            }
        }

        return dispute;
    }

    @Override
    public Dispute getDispute(String disputeId) {
        return disputeRepository.findById(disputeId)
                .orElseThrow(() -> new RuntimeException("Dispute not found"));
    }

    @Override
    public Dispute getDisputeByOrder(String orderId) {
        return disputeRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Dispute not found for order"));
    }

    @Override
    public Optional<Dispute> findOptionalByOrderId(String orderId) {
        return disputeRepository.findByOrderId(orderId);
    }

    @Override
    public List<Dispute> getAllDisputes() {
        return disputeRepository.findAll();
    }
}
