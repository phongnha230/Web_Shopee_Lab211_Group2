package com.shoppeclone.backend.shipping.controller;

import com.shoppeclone.backend.order.entity.Order;
import com.shoppeclone.backend.order.entity.OrderShipping;
import com.shoppeclone.backend.order.entity.OrderStatus;
import com.shoppeclone.backend.order.entity.PaymentStatus;
import com.shoppeclone.backend.order.repository.OrderRepository;
import com.shoppeclone.backend.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebhookControllerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private WebhookController webhookController;

    @Test
    void receiveShippingUpdate_usesOrderServiceWhenDeliveredSoSoldCanSync() {
        Order persistedOrder = new Order();
        persistedOrder.setId("order-1");
        persistedOrder.setOrderStatus(OrderStatus.SHIPPED);
        OrderShipping shipping = new OrderShipping();
        shipping.setTrackingCode("TRACK-1");
        persistedOrder.setShipping(shipping);

        Order completedOrder = new Order();
        completedOrder.setId("order-1");
        completedOrder.setOrderStatus(OrderStatus.COMPLETED);
        OrderShipping completedShipping = new OrderShipping();
        completedShipping.setTrackingCode("TRACK-1");
        completedOrder.setShipping(completedShipping);

        WebhookController.ShippingUpdatePayload payload = new WebhookController.ShippingUpdatePayload();
        payload.setTrackingCode("TRACK-1");
        payload.setStatus("DELIVERED");

        when(orderRepository.findByTrackingCode("TRACK-1")).thenReturn(Optional.of(persistedOrder));
        when(orderService.updateOrderStatus("order-1", OrderStatus.COMPLETED)).thenReturn(completedOrder);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        webhookController.receiveShippingUpdate(payload);

        verify(orderService).updateOrderStatus("order-1", OrderStatus.COMPLETED);
        verify(orderRepository).save(completedOrder);
        assertThat(completedOrder.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(completedOrder.getShipping().getStatus()).isEqualTo("DELIVERED");
        assertThat(completedOrder.getShipping().getDeliveredAt()).isNotNull();
    }

    @Test
    void receiveShippingUpdate_usesOrderServiceWhenReturnedSoStockCanRestore() {
        Order persistedOrder = new Order();
        persistedOrder.setId("order-2");
        persistedOrder.setOrderStatus(OrderStatus.SHIPPED);
        OrderShipping persistedShipping = new OrderShipping();
        persistedShipping.setTrackingCode("TRACK-2");
        persistedOrder.setShipping(persistedShipping);

        Order cancelledOrder = new Order();
        cancelledOrder.setId("order-2");
        cancelledOrder.setOrderStatus(OrderStatus.CANCELLED);
        cancelledOrder.setCancelReason("Buyer rejected package");
        OrderShipping cancelledShipping = new OrderShipping();
        cancelledShipping.setTrackingCode("TRACK-2");
        cancelledOrder.setShipping(cancelledShipping);

        WebhookController.ShippingUpdatePayload payload = new WebhookController.ShippingUpdatePayload();
        payload.setTrackingCode("TRACK-2");
        payload.setStatus("RETURNED");
        payload.setReason("Buyer rejected package");

        when(orderRepository.findByTrackingCode("TRACK-2")).thenReturn(Optional.of(persistedOrder));
        when(orderService.markOrderAsDeliveryFailed("order-2", "Buyer rejected package")).thenReturn(cancelledOrder);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        webhookController.receiveShippingUpdate(payload);

        verify(orderService).markOrderAsDeliveryFailed("order-2", "Buyer rejected package");
        verify(orderRepository).save(cancelledOrder);
        assertThat(cancelledOrder.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(cancelledOrder.getCancelReason()).isEqualTo("Buyer rejected package");
        assertThat(cancelledOrder.getShipping().getStatus()).isEqualTo("RETURNED");
    }
}

