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
    public ResponseEntity<Dispute> reviewDispute(@PathVariable String id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        String adminNote = body.get("adminNote");
        return ResponseEntity.ok(disputeService.updateDisputeStatus(id, status, adminNote));
    }
}
