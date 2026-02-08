package com.shoppeclone.backend.payment.controller;

import com.shoppeclone.backend.payment.entity.Payment;
import com.shoppeclone.backend.payment.entity.PaymentMethod;
import com.shoppeclone.backend.payment.repository.PaymentMethodRepository;
import com.shoppeclone.backend.payment.repository.PaymentRepository;
import com.shoppeclone.backend.payment.service.PaymentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentRepository paymentRepository;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createPayment(@RequestBody CreatePaymentRequest request) {
        PaymentMethod method = paymentMethodRepository.findByCode(request.getPaymentMethod())
                .orElseThrow(() -> new RuntimeException("Payment method not found: " + request.getPaymentMethod()));

        List<Payment> existing = paymentRepository.findByOrderId(request.getOrderId());
        Payment payment;
        if (!existing.isEmpty()) {
            payment = existing.get(0);
        } else {
            payment = paymentService.createPayment(request.getOrderId(), method.getId(), BigDecimal.ZERO);
        }

        return ResponseEntity.ok(Map.of(
                "id", payment.getId(),
                "orderId", payment.getOrderId(),
                "redirectUrl", (Object) null
        ));
    }

    @Data
    public static class CreatePaymentRequest {
        private String orderId;
        private String paymentMethod;
    }

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
