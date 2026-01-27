package com.shoppeclone.backend.dispute.service;

import com.shoppeclone.backend.dispute.entity.Dispute;
import java.util.List;

public interface DisputeService {
    Dispute createDispute(String orderId, String buyerId, String reason, String description);

    Dispute updateDisputeStatus(String disputeId, String status, String adminNote);

    com.shoppeclone.backend.dispute.entity.DisputeImage addDisputeImage(String disputeId, String imageUrl,
            String uploadedBy);

    List<com.shoppeclone.backend.dispute.entity.DisputeImage> getDisputeImages(String disputeId);

    Dispute getDispute(String disputeId);

    Dispute getDisputeByOrder(String orderId);

    List<Dispute> getAllDisputes();
}
