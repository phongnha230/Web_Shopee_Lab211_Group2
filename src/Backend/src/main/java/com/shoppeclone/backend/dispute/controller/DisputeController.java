package com.shoppeclone.backend.dispute.controller;

import com.shoppeclone.backend.dispute.entity.Dispute;
import com.shoppeclone.backend.dispute.service.DisputeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/disputes")
@RequiredArgsConstructor
public class DisputeController {

    private final DisputeService disputeService;

    @PostMapping
    public ResponseEntity<Dispute> createDispute(@RequestBody Map<String, String> requestBody) {
        String orderId = requestBody.get("orderId");
        String buyerId = requestBody.get("buyerId"); // In real app, get from Context
        String reason = requestBody.get("reason");
        String description = requestBody.get("description");

        return ResponseEntity.ok(disputeService.createDispute(orderId, buyerId, reason, description));
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<com.shoppeclone.backend.dispute.entity.DisputeImage> uploadImage(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String imageUrl = body.get("imageUrl");
        String uploadedBy = body.get("uploadedBy");

        return ResponseEntity.ok(disputeService.addDisputeImage(id, imageUrl, uploadedBy));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Dispute> getDispute(@PathVariable String id) {
        return ResponseEntity.ok(disputeService.getDispute(id));
    }
}
