package com.shoppeclone.backend.refund.controller;

import com.shoppeclone.backend.refund.entity.Refund;
import com.shoppeclone.backend.refund.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @PostMapping("/{orderId}/request")
    public ResponseEntity<Refund> requestRefund(@PathVariable String orderId,
            @RequestBody Map<String, Object> requestBody) {
        String buyerId = (String) requestBody.get("buyerId");
        String reason = (String) requestBody.get("reason");
        // Handle amount safely from Number/Integer/Double/String
        Object amountObj = requestBody.get("amount");
        BigDecimal amount = BigDecimal.ZERO;
        if (amountObj != null) {
            amount = new BigDecimal(amountObj.toString());
        }

        return ResponseEntity.ok(refundService.createRefund(orderId, buyerId, amount, reason));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Refund> getRefundByOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(refundService.getRefundByOrderId(orderId));
    }
}
