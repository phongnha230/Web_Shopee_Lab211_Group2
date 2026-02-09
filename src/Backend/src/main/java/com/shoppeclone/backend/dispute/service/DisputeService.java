package com.shoppeclone.backend.dispute.service;

import com.shoppeclone.backend.dispute.entity.Dispute;
import java.util.List;
import java.util.Optional;

public interface DisputeService {
    Dispute createDispute(String orderId, String buyerId, String reason, String description);

    Dispute updateDisputeStatus(String disputeId, String status, String adminNote);

    /** Cập nhật dispute, nếu RESOLVED + approveRefund=true thì tự động tạo Refund APPROVED */
    Dispute updateDisputeStatusWithRefund(String disputeId, String status, String adminNote, boolean approveRefund, java.math.BigDecimal refundAmount, String adminId);

    com.shoppeclone.backend.dispute.entity.DisputeImage addDisputeImage(String disputeId, String imageUrl,
            String uploadedBy);

    List<com.shoppeclone.backend.dispute.entity.DisputeImage> getDisputeImages(String disputeId);

    Dispute getDispute(String disputeId);

    Dispute getDisputeByOrder(String orderId);

    Optional<Dispute> findOptionalByOrderId(String orderId);

    List<Dispute> getAllDisputes();
}
