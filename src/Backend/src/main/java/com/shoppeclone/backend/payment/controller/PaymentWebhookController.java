package com.shoppeclone.backend.payment.controller;

import com.shoppeclone.backend.order.entity.Order;
import com.shoppeclone.backend.order.entity.PaymentStatus;
import com.shoppeclone.backend.order.repository.OrderRepository;
import com.shoppeclone.backend.payment.entity.Payment;
import com.shoppeclone.backend.payment.repository.PaymentRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Webhook controller for payment gateway callbacks (Momo, VNPAY, etc.)
 * When customer pays electronically, gateway sends IPN to update order status.
 */
@RestController
@RequestMapping("/api/webhooks/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookController {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    /**
     * Momo IPN (Instant Payment Notification) callback.
     * Momo sends POST with JSON when payment completes.
     * Must respond with HTTP 204 within 15 seconds.
     * @see <a href="https://developers.momo.vn/v3/docs/payment/api/result-handling/notification/">Momo IPN Docs</a>
     */
    @PostMapping("/momo")
    public ResponseEntity<Void> momoIpn(@RequestBody MomoIpnPayload payload) {
        log.info("Momo IPN received: orderId={}, resultCode={}, amount={}", 
                payload.getOrderId(), payload.getResultCode(), payload.getAmount());

        // resultCode: 0 = success, 9000 = authorized
        if (payload.getResultCode() != null && (payload.getResultCode() == 0 || payload.getResultCode() == 9000)) {
            String orderId = payload.getOrderId();
            if (orderId == null || orderId.isEmpty()) {
                log.warn("Momo IPN: orderId is empty");
                return ResponseEntity.noContent().build();
            }

            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                log.warn("Momo IPN: Order not found: {}", orderId);
                return ResponseEntity.noContent().build();
            }

            // Update Order.paymentStatus
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            // Update Payment entity
            List<Payment> payments = paymentRepository.findByOrderId(orderId);
            for (Payment p : payments) {
                p.setStatus(com.shoppeclone.backend.payment.entity.PaymentStatus.PAID);
                p.setPaidAt(LocalDateTime.now());
                paymentRepository.save(p);
            }

            log.info("Momo IPN: Order {} marked as PAID", orderId);
        } else {
            log.info("Momo IPN: Payment failed or pending, resultCode={}", payload.getResultCode());
        }

        // Momo requires 204 No Content response
        return ResponseEntity.noContent().build();
    }

    /**
     * VNPAY IPN callback (similar structure for future use)
     */
    @PostMapping("/vnpay")
    public ResponseEntity<Void> vnpayIpn(@RequestBody VnpayIpnPayload payload) {
        log.info("VNPAY IPN received: orderId={}, vnp_ResponseCode={}", 
                payload.getVnp_TxnRef(), payload.getVnp_ResponseCode());

        // vnp_ResponseCode = "00" means success
        if ("00".equals(payload.getVnp_ResponseCode())) {
            String orderId = payload.getVnp_TxnRef();
            if (orderId != null && !orderId.isEmpty()) {
                Order order = orderRepository.findById(orderId).orElse(null);
                if (order != null) {
                    order.setPaymentStatus(PaymentStatus.PAID);
                    order.setUpdatedAt(LocalDateTime.now());
                    orderRepository.save(order);

                    List<Payment> payments = paymentRepository.findByOrderId(orderId);
                    for (Payment p : payments) {
                        p.setStatus(com.shoppeclone.backend.payment.entity.PaymentStatus.PAID);
                        p.setPaidAt(LocalDateTime.now());
                        paymentRepository.save(p);
                    }
                    log.info("VNPAY IPN: Order {} marked as PAID", orderId);
                }
            }
        }

        return ResponseEntity.noContent().build();
    }

    @Data
    public static class MomoIpnPayload {
        private String partnerCode;
        private String orderId;      // Partner's order/transaction ID
        private Long amount;
        private Integer resultCode;  // 0 = success, 9000 = authorized
        private String message;
        private String transId;
        private String requestId;
        private Long responseTime;
        private String signature;
        private String orderInfo;
        private String extraData;
        private String payType;
        private String orderType;
    }

    @Data
    public static class VnpayIpnPayload {
        private String vnp_TxnRef;       // Order ID
        private String vnp_ResponseCode; // "00" = success
        private String vnp_TransactionNo;
        private String vnp_Amount;
        private String vnp_SecureHash;
        // ... other VNPAY fields as needed
    }
}
