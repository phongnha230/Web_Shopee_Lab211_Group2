package com.shoppeclone.backend.payment.service;

import com.shoppeclone.backend.payment.entity.Payment;
import com.shoppeclone.backend.payment.entity.PaymentMethod;

import java.util.List;

public interface PaymentService {
    List<PaymentMethod> getAllPaymentMethods();

    Payment createPayment(String orderId, String paymentMethodId, java.math.BigDecimal amount);

    Payment getPaymentByOrderId(String orderId);

    void updatePaymentStatus(String paymentId, String status); // status: PAID, FAILED
}
