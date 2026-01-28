package com.shoppeclone.backend.refund.service;

import com.shoppeclone.backend.refund.entity.Refund;
import java.math.BigDecimal;
import java.util.List;

public interface RefundService {
    Refund createRefund(String orderId, String buyerId, BigDecimal amount, String reason);

    Refund approveRefund(String refundId, String adminId);

    Refund rejectRefund(String refundId, String adminId);

    List<Refund> getAllRefunds();

    Refund getRefundByOrderId(String orderId);
}
