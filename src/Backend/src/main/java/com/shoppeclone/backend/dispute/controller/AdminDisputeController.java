package com.shoppeclone.backend.dispute.controller;

import com.shoppeclone.backend.dispute.entity.Dispute;
import com.shoppeclone.backend.dispute.service.DisputeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/disputes")
@RequiredArgsConstructor
public class AdminDisputeController {

    private final DisputeService disputeService;

    @GetMapping
    public ResponseEntity<List<Dispute>> getAllDisputes() {
        return ResponseEntity.ok(disputeService.getAllDisputes());
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<Dispute> reviewDispute(@PathVariable String id, @RequestBody Map<String, Object> body) {
        String status = body.get("status") != null ? body.get("status").toString() : null;
        String adminNote = body.get("adminNote") != null ? body.get("adminNote").toString() : null;
        Object approveRefundObj = body.get("approveRefund");
        boolean approveRefund = approveRefundObj instanceof Boolean && (Boolean) approveRefundObj
                || "true".equalsIgnoreCase(String.valueOf(approveRefundObj));
        java.math.BigDecimal refundAmount = null;
        if (body.get("refundAmount") != null) {
            try {
                refundAmount = new java.math.BigDecimal(body.get("refundAmount").toString());
            } catch (NumberFormatException ignored) {}
        }
        String adminId = body.get("adminId") != null ? body.get("adminId").toString() : "ADMIN_USER";

        if (approveRefund && "RESOLVED".equalsIgnoreCase(status)) {
            return ResponseEntity.ok(disputeService.updateDisputeStatusWithRefund(id, status, adminNote, true, refundAmount, adminId));
        }
        return ResponseEntity.ok(disputeService.updateDisputeStatus(id, status, adminNote));
    }
}
