package com.shoppeclone.backend.refund.service;

import com.shoppeclone.backend.refund.entity.Refund;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface RefundService {
    Refund createRefund(String orderId, String buyerId, BigDecimal amount, String reason);

    /** Tạo refund đã duyệt (từ Dispute resolution) */
    Refund createAndApproveRefund(String orderId, String buyerId, BigDecimal amount, String reason, String adminId);

    Refund approveRefund(String refundId, String adminId);

    Refund rejectRefund(String refundId, String adminId);

    List<Refund> getAllRefunds();

    Refund getRefundByOrderId(String orderId);

    Optional<Refund> findOptionalByOrderId(String orderId);
}
