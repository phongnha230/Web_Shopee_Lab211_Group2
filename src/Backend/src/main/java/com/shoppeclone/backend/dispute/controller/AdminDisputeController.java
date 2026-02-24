package com.shoppeclone.backend.dispute.controller;

import com.shoppeclone.backend.dispute.dto.request.AdminReviewDisputeRequest;
import com.shoppeclone.backend.dispute.entity.Dispute;
import com.shoppeclone.backend.dispute.service.DisputeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/disputes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDisputeController {

    private final DisputeService disputeService;

    @GetMapping
    public ResponseEntity<List<Dispute>> getAllDisputes() {
        return ResponseEntity.ok(disputeService.getAllDisputes());
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<Dispute> reviewDispute(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id,
            @Valid @RequestBody AdminReviewDisputeRequest body) {
        String status = body.getStatus();
        String adminNote = body.getAdminNote();
        boolean approveRefund = Boolean.TRUE.equals(body.getApproveRefund());
        String adminId = userDetails != null ? userDetails.getUsername() : "ADMIN_USER";

        if (approveRefund && "RESOLVED".equalsIgnoreCase(status)) {
            return ResponseEntity.ok(
                    disputeService.updateDisputeStatusWithRefund(id, status, adminNote, true, body.getRefundAmount(), adminId));
        }
        return ResponseEntity.ok(disputeService.updateDisputeStatus(id, status, adminNote));
    }
}
