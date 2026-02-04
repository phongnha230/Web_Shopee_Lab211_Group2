package com.shoppeclone.backend.order.service.impl;

import com.shoppeclone.backend.cart.entity.Cart;
import com.shoppeclone.backend.cart.entity.CartItem;
import com.shoppeclone.backend.cart.repository.CartRepository;
import com.shoppeclone.backend.cart.service.CartService;
import com.shoppeclone.backend.order.dto.OrderRequest;
import com.shoppeclone.backend.order.entity.*;
import com.shoppeclone.backend.promotion.entity.Voucher;
import com.shoppeclone.backend.order.repository.OrderRepository;
import com.shoppeclone.backend.order.service.OrderService;
import com.shoppeclone.backend.product.entity.ProductVariant;
import com.shoppeclone.backend.product.repository.ProductRepository;
import com.shoppeclone.backend.product.repository.ProductVariantRepository;
import com.shoppeclone.backend.shipping.entity.ShippingProvider;
import com.shoppeclone.backend.shipping.repository.ShippingProviderRepository;
import com.shoppeclone.backend.shipping.service.ShippingService;
import com.shoppeclone.backend.payment.service.PaymentService;
import com.shoppeclone.backend.payment.repository.PaymentMethodRepository;
import com.shoppeclone.backend.payment.entity.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
<<<<<<< HEAD
    private final CartRepository cartRepository;
=======
    private final ProductRepository productRepository;
>>>>>>> 7518d80b4b75ba767f98131898e96c9b5a32886c
    private final ProductVariantRepository productVariantRepository;
    private final ShippingProviderRepository shippingProviderRepository;
    private final ShippingService shippingService;
    private final PaymentService paymentService;
    private final PaymentMethodRepository paymentMethodRepository;
    private final com.shoppeclone.backend.promotion.repository.VoucherRepository voucherRepository;

    @Override
    @Transactional
    public Order createOrder(String userId, OrderRequest request) {
        // 1. Get Cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // 2. Filter items to checkout
        List<CartItem> checkoutItems;
        if (request.getVariantIds() != null && !request.getVariantIds().isEmpty()) {
            checkoutItems = cart.getItems().stream()
                    .filter(item -> request.getVariantIds().contains(item.getVariantId()))
                    .collect(Collectors.toList());
        } else {
            checkoutItems = new ArrayList<>(cart.getItems());
        }

        if (checkoutItems.isEmpty()) {
            throw new RuntimeException("No items selected for checkout");
        }

        // 3. Create Order Items and Deduct Stock
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;
        String shopId = null; // Will be set from first variant

        for (CartItem cartItem : checkoutItems) {
            ProductVariant variant = productVariantRepository.findById(cartItem.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found: " + cartItem.getVariantId()));

            if (variant.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Not enough stock for variant: " + variant.getId());
            }

            // Get shopId from product (all items should be from same shop)
            if (shopId == null) {
                com.shoppeclone.backend.product.entity.Product product = productRepository
                        .findById(variant.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found"));
                shopId = product.getShopId();
            }

            // Deduct stock
            variant.setStock(variant.getStock() - cartItem.getQuantity());
            productVariantRepository.save(variant);

            // Create Order Item
            OrderItem orderItem = new OrderItem();
            orderItem.setVariantId(variant.getId());
            orderItem.setPrice(variant.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItems.add(orderItem);

            totalPrice = totalPrice.add(variant.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        // 4. Calculate Discount (Product Voucher)
        BigDecimal productDiscount = BigDecimal.ZERO;
        String appliedProductVoucherCode = null;

        if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {
            appliedProductVoucherCode = processVoucher(request.getVoucherCode(), totalPrice,
                    Voucher.VoucherType.PRODUCT);
            productDiscount = calculateDiscount(appliedProductVoucherCode, totalPrice);
        }

        // 5. Shipping Calculation (moved before shipping voucher processing)
        BigDecimal shippingFee = BigDecimal.ZERO;
        ShippingProvider provider = null;
        if (request.getShippingProviderId() != null) {
            provider = shippingProviderRepository.findById(request.getShippingProviderId())
                    .orElseThrow(() -> new RuntimeException("Shipping Provider not found"));
            shippingFee = shippingService.calculateShippingFee(provider.getId(), request.getAddress(), totalPrice);
        }

        // 6. Calculate Shipping Discount (Shipping Voucher)
        BigDecimal shippingDiscount = BigDecimal.ZERO;
        String appliedShippingVoucherCode = null;

        if (request.getShippingVoucherCode() != null && !request.getShippingVoucherCode().trim().isEmpty()) {
            if (shippingFee.compareTo(BigDecimal.ZERO) > 0) {
                appliedShippingVoucherCode = processVoucher(request.getShippingVoucherCode(), totalPrice,
                        Voucher.VoucherType.SHIPPING);
                shippingDiscount = calculateDiscount(appliedShippingVoucherCode, shippingFee); // Apply against Shipping
                                                                                               // Fee, but minSpend
                                                                                               // checks Subtotal
                                                                                               // (totalPrice)
            }
        }

        // 7. Create Order
        Order order = new Order();
        order.setUserId(userId);
        order.setShopId(shopId); // Set shopId
        order.setItems(orderItems);
        order.setTotalPrice(totalPrice.subtract(productDiscount).add(shippingFee).subtract(shippingDiscount)); // (Subtotal
                                                                                                               // -
                                                                                                               // ProdDisc)
                                                                                                               // +
                                                                                                               // (ShipFee
                                                                                                               // -
                                                                                                               // ShipDisc)
        order.setDiscount(productDiscount);
        order.setVoucherCode(appliedProductVoucherCode);
        order.setShippingDiscount(shippingDiscount);
        order.setShippingVoucherCode(appliedShippingVoucherCode);

        order.setOrderStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.UNPAID);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // 8. Shipping Info
        if (provider != null) {
            OrderShipping shipping = new OrderShipping();
            shipping.setProviderId(provider.getId());
            shipping.setAddress(request.getAddress());
            shipping.setStatus("WAITING");
            shipping.setShippingFee(shippingFee);
            order.setShipping(shipping);
        }

        // 9. Save Order
        Order savedOrder = orderRepository.save(order);

        // 10. Remove ordered items from Cart
        for (CartItem item : checkoutItems) {
            cartService.removeCartItem(userId, item.getVariantId());
        }

        // 11. Create Payment
        if (request.getPaymentMethod() != null) {
            PaymentMethod paymentMethod = paymentMethodRepository.findByCode(request.getPaymentMethod())
                    .orElseThrow(() -> new RuntimeException("Payment method not found: " + request.getPaymentMethod()));

            paymentService.createPayment(savedOrder.getId(), paymentMethod.getId(), savedOrder.getTotalPrice());
        }

        return savedOrder;
    }

    // Helper to validate voucher and return code if valid
    private String processVoucher(String code, BigDecimal orderValue, Voucher.VoucherType expectedType) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Voucher not found: " + code));

        // Validate Type
        if (voucher.getType() != null && voucher.getType() != expectedType) {
            throw new RuntimeException(
                    "Invalid voucher type. Expected " + expectedType + " but got " + voucher.getType());
        }

        // Validate Status
        LocalDateTime now = LocalDateTime.now();
        if (!voucher.isActive()
                || (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate()))
                || (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate()))) {
            throw new RuntimeException("Voucher is expired or inactive");
        }

        if (voucher.getQuantity() <= 0) {
            throw new RuntimeException("Voucher is out of stock");
        }

        // Min Spend Check (Always checks against Subtotal/OrderValue)
        if (voucher.getMinSpend() != null && orderValue.compareTo(voucher.getMinSpend()) < 0) {
            throw new RuntimeException(
                    "Order total does not meet minimum spend for voucher: " + voucher.getMinSpend());
        }

        // Update Usage (Optimistic)
        voucher.setQuantity(voucher.getQuantity() - 1);
        voucher.setUsedCount(voucher.getUsedCount() == null ? 1 : voucher.getUsedCount() + 1);
        voucherRepository.save(voucher);

        return code;
    }

    private BigDecimal calculateDiscount(String code, BigDecimal baseAmount) {
        if (code == null)
            return BigDecimal.ZERO;

        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Voucher not found during calc: " + code));

        BigDecimal discount = BigDecimal.ZERO;

        if (voucher.getDiscountType() == Voucher.DiscountType.PERCENTAGE) {
            discount = baseAmount.multiply(voucher.getDiscountValue()).divide(BigDecimal.valueOf(100));
            if (voucher.getMaxDiscount() != null && discount.compareTo(voucher.getMaxDiscount()) > 0) {
                discount = voucher.getMaxDiscount();
            }
        } else {
            discount = voucher.getDiscountValue();
        }

        // Cap discount at baseAmount
        if (discount.compareTo(baseAmount) > 0) {
            discount = baseAmount;
        }

        return discount;
    }

    @Override
    public Order getOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Override
    public List<Order> getUserOrders(String userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public List<Order> getOrdersByShopId(String shopId, OrderStatus status) {
        if (status == null) {
            return orderRepository.findByShopId(shopId);
        }
        return orderRepository.findByShopIdAndOrderStatus(shopId, status);
    }

    @Override
    public Order updateOrderStatus(String orderId, OrderStatus status) {
        Order order = getOrder(orderId);

        // Update timestamps based on status
        if (status == OrderStatus.SHIPPED && order.getShippedAt() == null) {
            order.setShippedAt(LocalDateTime.now());
        }
        if (status == OrderStatus.COMPLETED && order.getCompletedAt() == null) {
            order.setCompletedAt(LocalDateTime.now());
        }

        order.setOrderStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order cancelOrder(String orderId) {
        Order order = getOrder(orderId);
        if (order.getOrderStatus() == OrderStatus.COMPLETED || order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Cannot cancel order in state: " + order.getOrderStatus());
        }

        // Restore Stock
        for (OrderItem item : order.getItems()) {
            ProductVariant variant = productVariantRepository.findById(item.getVariantId()).orElse(null);
            if (variant != null) {
                variant.setStock(variant.getStock() + item.getQuantity());
                productVariantRepository.save(variant);
            }
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now()); // Set cancelledAt
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order updateShipment(String orderId, String trackingCode, String providerId) {
        Order order = getOrder(orderId);

        if (order.getShipping() == null) {
            OrderShipping shipping = new OrderShipping();
            shipping.setStatus("WAITING");
            order.setShipping(shipping);
        }

        OrderShipping shipping = order.getShipping();
        if (trackingCode != null)
            shipping.setTrackingCode(trackingCode);
        if (providerId != null)
            shipping.setProviderId(providerId);

        // Auto update status to SHIPPED if tracking code is present
        if (trackingCode != null && !trackingCode.isEmpty()) {
            shipping.setStatus("SHIPPED"); // Assuming string status in OrderShipping matches
            shipping.setShippedAt(LocalDateTime.now());
            order.setOrderStatus(OrderStatus.SHIPPED);
            if (order.getShippedAt() == null) {
                order.setShippedAt(LocalDateTime.now());
            }
        }

        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order assignShipper(String orderId, String shipperId) {
        Order order = getOrder(orderId);

        order.setShipperId(shipperId);
        order.setAssignedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }
}
