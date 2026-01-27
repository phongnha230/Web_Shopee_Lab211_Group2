package com.shoppeclone.backend.payment.service.impl;

import com.shoppeclone.backend.payment.entity.Payment;
import com.shoppeclone.backend.payment.entity.PaymentMethod;
import com.shoppeclone.backend.payment.entity.PaymentStatus;
import com.shoppeclone.backend.payment.repository.PaymentMethodRepository;
import com.shoppeclone.backend.payment.repository.PaymentRepository;
import com.shoppeclone.backend.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public List<PaymentMethod> getAllPaymentMethods() {
        return paymentMethodRepository.findAll();
    }

    @Override
    public Payment createPayment(String orderId, String paymentMethodId, java.math.BigDecimal amount) {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setPaymentMethodId(paymentMethodId);
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.PENDING);
        // paidAt is null initially
        return paymentRepository.save(payment);
    }

    @Override
    public Payment getPaymentByOrderId(String orderId) {
        // Assuming one payment per order for simplicity, or get the latest
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        if (payments.isEmpty()) {
            return null;
        }
        return payments.get(0); // Return the first one for now
    }

    @Override
    public void updatePaymentStatus(String paymentId, String statusString) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        try {
            PaymentStatus status = PaymentStatus.valueOf(statusString.toUpperCase());
            payment.setStatus(status);
            if (status == PaymentStatus.PAID) {
                payment.setPaidAt(LocalDateTime.now());
            }
            paymentRepository.save(payment);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid payment status: " + statusString);
        }
    }
}
