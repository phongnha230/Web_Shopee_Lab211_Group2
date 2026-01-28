package com.shoppeclone.backend.refund.controller;

import com.shoppeclone.backend.refund.entity.Refund;
import com.shoppeclone.backend.refund.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/refunds")
@RequiredArgsConstructor
public class AdminRefundController {

    private final RefundService refundService;

    @GetMapping
    public ResponseEntity<List<Refund>> getAllRefunds() {
        return ResponseEntity.ok(refundService.getAllRefunds());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Refund> approveRefund(@PathVariable String id,
            @RequestParam(required = false) String adminId) {
        // In a real scenario, extract adminId from SecurityContext
        if (adminId == null) {
            adminId = "ADMIN_USER"; // Placeholder
        }
        return ResponseEntity.ok(refundService.approveRefund(id, adminId));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Refund> rejectRefund(@PathVariable String id,
            @RequestParam(required = false) String adminId) {
        // In a real scenario, extract adminId from SecurityContext
        if (adminId == null) {
            adminId = "ADMIN_USER";
        }
        return ResponseEntity.ok(refundService.rejectRefund(id, adminId));
    }
}
