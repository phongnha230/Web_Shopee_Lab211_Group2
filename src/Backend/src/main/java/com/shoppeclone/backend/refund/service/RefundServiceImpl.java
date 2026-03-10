package com.shoppeclone.backend.refund.service;

import com.shoppeclone.backend.order.service.OrderService;
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
    private final OrderService orderService;

    @Override
    public Refund createRefund(String orderId, String buyerId, BigDecimal amount, String reason) {
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
            if (refund.getStatus() == RefundStatus.APPROVED || refund.getStatus() == RefundStatus.REFUNDED) {
                return refund;
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

        orderService.markOrderAsReturned(orderId);

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

        orderService.markOrderAsReturned(refund.getOrderId());

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
}
