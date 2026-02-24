package com.shoppeclone.backend.refund.controller;

import com.shoppeclone.backend.refund.entity.Refund;
import com.shoppeclone.backend.refund.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/refunds")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRefundController {

    private final RefundService refundService;

    @GetMapping
    public ResponseEntity<List<Refund>> getAllRefunds() {
        return ResponseEntity.ok(refundService.getAllRefunds());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Refund> approveRefund(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id) {
        String adminId = userDetails != null ? userDetails.getUsername() : "ADMIN_USER";
        return ResponseEntity.ok(refundService.approveRefund(id, adminId));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Refund> rejectRefund(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id) {
        String adminId = userDetails != null ? userDetails.getUsername() : "ADMIN_USER";
        return ResponseEntity.ok(refundService.rejectRefund(id, adminId));
    }
}
