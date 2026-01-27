package com.shoppeclone.backend.payment.controller;

import com.shoppeclone.backend.payment.entity.Payment;
import com.shoppeclone.backend.payment.entity.PaymentMethod;
import com.shoppeclone.backend.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/methods")
    public ResponseEntity<List<PaymentMethod>> getPaymentMethods() {
        return ResponseEntity.ok(paymentService.getAllPaymentMethods());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Payment> getPaymentByOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }

    // Internal use or callback for payment status updates (simplified)
    // In real world, this would be a webhook from payment provider
    @PostMapping("/{paymentId}/status")
    public ResponseEntity<Void> updatePaymentStatus(@PathVariable String paymentId, @RequestParam String status) {
        paymentService.updatePaymentStatus(paymentId, status);
        return ResponseEntity.ok().build();
    }
}
