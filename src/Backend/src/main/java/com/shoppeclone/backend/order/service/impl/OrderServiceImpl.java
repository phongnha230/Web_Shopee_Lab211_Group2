package com.shoppeclone.backend.order.service.impl;

// Trigger recompile

import com.shoppeclone.backend.cart.entity.Cart;
import com.shoppeclone.backend.cart.entity.CartItem;
import com.shoppeclone.backend.cart.repository.CartRepository;
import com.shoppeclone.backend.cart.service.CartService;
import com.shoppeclone.backend.order.dto.OrderItemRequest;
import com.shoppeclone.backend.order.dto.OrderRequest;
import com.shoppeclone.backend.order.dto.ShippingAddressRequest;
import com.shoppeclone.backend.order.entity.*;
import com.shoppeclone.backend.promotion.entity.ShopVoucher;
import com.shoppeclone.backend.promotion.entity.Voucher;
import com.shoppeclone.backend.promotion.repository.ShopVoucherRepository;
import com.shoppeclone.backend.order.repository.OrderRepository;
import com.shoppeclone.backend.order.service.OrderService;
import com.shoppeclone.backend.product.entity.ProductCategory;
import com.shoppeclone.backend.product.entity.ProductVariant;
import com.shoppeclone.backend.product.repository.ProductCategoryRepository;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ShippingProviderRepository shippingProviderRepository;
    private final ShippingService shippingService;
    private final PaymentService paymentService;
    private final PaymentMethodRepository paymentMethodRepository;
    private final com.shoppeclone.backend.promotion.repository.VoucherRepository voucherRepository;
    private final ShopVoucherRepository shopVoucherRepository;
    private final com.shoppeclone.backend.promotion.flashsale.repository.FlashSaleItemRepository flashSaleItemRepository;
    private final com.shoppeclone.backend.promotion.flashsale.repository.FlashSaleRepository flashSaleRepository;

    @Override
    @Transactional
    public Order createOrder(String userId, OrderRequest request) {
        // Resolve address: prefer shippingAddress (from frontend) over address
        com.shoppeclone.backend.user.model.Address resolvedAddress = resolveAddress(request);

        // 1. Buy Now mode: use request.items directly
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            return createOrderFromItems(userId, request, resolvedAddress);
        }

        // 2. Cart mode: get items from cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // 3. Filter items to checkout
        List<CartItem> checkoutItems;
        if (request.getVariantIds() != null && !request.getVariantIds().isEmpty()) {
            checkoutItems = cart.getItems().stream()
                    .filter(item -> request.getVariantIds().contains(item.getVariantId()))
                    .collect(Collectors.toList());
        } else {
            checkoutItems = new ArrayList<>(cart.getItems());
        }

        // 4. Remove stale items (variants that no longer exist) and clean cart
        List<String> invalidVariantIds = checkoutItems.stream()
                .filter(item -> !productVariantRepository.existsById(item.getVariantId()))
                .map(CartItem::getVariantId)
                .collect(Collectors.toList());
        checkoutItems = checkoutItems.stream()
                .filter(item -> productVariantRepository.existsById(item.getVariantId()))
                .collect(Collectors.toList());

        for (String invalidId : invalidVariantIds) {
            cartService.removeCartItem(userId, invalidId);
        }

        if (checkoutItems.isEmpty()) {
            throw new RuntimeException(
                    "No valid items to checkout. Your cart may contain removed products. Please refresh your cart.");
        }

        // 5. Create Order Items and Deduct Stock
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;
        String shopId = null;

        for (CartItem cartItem : checkoutItems) {
            ProductVariant variant = productVariantRepository.findById(cartItem.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found: " + cartItem.getVariantId()));

            if (variant.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Not enough stock for variant: " + variant.getId());
            }

            if (shopId == null) {
                com.shoppeclone.backend.product.entity.Product product = productRepository
                        .findById(variant.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found"));
                shopId = product.getShopId();
            }

            variant.setStock(variant.getStock() - cartItem.getQuantity());

            // Update Flash Sale Sold Count
            // More resilient check: If the variant is NOT explicitly marked, but the
            // product IS in flash sale, treat it as flash sale
            com.shoppeclone.backend.product.entity.Product product = productRepository
                    .findById(variant.getProductId())
                    .orElse(null);

            boolean isActuallyFlashSale = Boolean.TRUE.equals(variant.getIsFlashSale()) ||
                    (product != null && Boolean.TRUE.equals(product.getIsFlashSale()));

            if (isActuallyFlashSale) {
                variant.setFlashSaleSold(
                        (variant.getFlashSaleSold() != null ? variant.getFlashSaleSold() : 0) + cartItem.getQuantity());

                // Ensure variant flag is synced if product flag is true (self-healing)
                if (product != null && Boolean.TRUE.equals(product.getIsFlashSale())) {
                    variant.setIsFlashSale(true);
                }

                // Also update Product level - removed strict product flag check to ensure
                // consistency with variant
                // Update FlashSaleItem remainingStock (for Homepage display)
                boolean stockUpdated = updateFlashSaleItemStock(variant.getProductId(), variant.getId(),
                        cartItem.getQuantity());

                if (stockUpdated) {
                    variant.setFlashSaleSold(
                            (variant.getFlashSaleSold() != null ? variant.getFlashSaleSold() : 0)
                                    + cartItem.getQuantity());
                    if (product != null) {
                        product.setFlashSaleSold((product.getFlashSaleSold() != null ? product.getFlashSaleSold() : 0)
                                + cartItem.getQuantity());
                        productRepository.save(product);
                    }
                }
            }

            productVariantRepository.save(variant);

            BigDecimal unitPrice = variant.getPrice();
            if (isActuallyFlashSale) {
                if (variant.getFlashSalePrice() != null) {
                    unitPrice = variant.getFlashSalePrice();
                } else if (product != null && product.getFlashSalePrice() != null) {
                    unitPrice = product.getFlashSalePrice();
                }
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setVariantId(variant.getId());
            orderItem.setPrice(unitPrice);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItems.add(orderItem);

            totalPrice = totalPrice.add(unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        return finishOrder(userId, request, orderItems, totalPrice, shopId, resolvedAddress, checkoutItems, cart);
    }

    /** Create order from direct items (Buy Now mode) - no cart involved */
    private Order createOrderFromItems(String userId, OrderRequest request,
            com.shoppeclone.backend.user.model.Address resolvedAddress) {
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;
        String shopId = null;

        for (OrderItemRequest itemReq : request.getItems()) {
            ProductVariant variant = productVariantRepository.findById(itemReq.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found: " + itemReq.getVariantId()));

            if (variant.getStock() < itemReq.getQuantity()) {
                throw new RuntimeException("Not enough stock for variant: " + variant.getId());
            }

            if (shopId == null) {
                com.shoppeclone.backend.product.entity.Product product = productRepository
                        .findById(variant.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found"));
                shopId = product.getShopId();
            }

            variant.setStock(variant.getStock() - itemReq.getQuantity());

            // Update Flash Sale Sold Count
            // More resilient check: If the variant is NOT explicitly marked, but the
            // product IS in flash sale, treat it as flash sale
            com.shoppeclone.backend.product.entity.Product product = productRepository
                    .findById(variant.getProductId())
                    .orElse(null);

            boolean isActuallyFlashSale = Boolean.TRUE.equals(variant.getIsFlashSale()) ||
                    (product != null && Boolean.TRUE.equals(product.getIsFlashSale()));

            if (isActuallyFlashSale) {
                // Update FlashSaleItem remainingStock (for Homepage display)
                boolean stockUpdated = updateFlashSaleItemStock(variant.getProductId(), variant.getId(),
                        itemReq.getQuantity());

                if (stockUpdated) {
                    variant.setFlashSaleSold(
                            (variant.getFlashSaleSold() != null ? variant.getFlashSaleSold() : 0)
                                    + itemReq.getQuantity());

                    // Also update Product level
                    if (product != null) {
                        product.setFlashSaleSold((product.getFlashSaleSold() != null ? product.getFlashSaleSold() : 0)
                                + itemReq.getQuantity());
                        productRepository.save(product);
                    }
                }
            }

            productVariantRepository.save(variant);

            BigDecimal unitPrice = variant.getPrice();
            if (isActuallyFlashSale) {
                if (variant.getFlashSalePrice() != null) {
                    unitPrice = variant.getFlashSalePrice();
                } else if (product != null && product.getFlashSalePrice() != null) {
                    unitPrice = product.getFlashSalePrice();
                }
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setVariantId(variant.getId());
            orderItem.setPrice(unitPrice);
            orderItem.setQuantity(itemReq.getQuantity());
            orderItems.add(orderItem);

            totalPrice = totalPrice.add(unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }

        return finishOrder(userId, request, orderItems, totalPrice, shopId, resolvedAddress, null, null);
    }

    private com.shoppeclone.backend.user.model.Address resolveAddress(OrderRequest request) {
        if (request.getAddress() != null) {
            return request.getAddress();
        }
        ShippingAddressRequest sa = request.getShippingAddress();
        if (sa == null) {
            throw new RuntimeException("Shipping address is required");
        }
        com.shoppeclone.backend.user.model.Address addr = new com.shoppeclone.backend.user.model.Address();
        addr.setFullName(sa.getFullName());
        addr.setPhone(sa.getPhone());
        addr.setAddress(sa.getStreet());
        addr.setCity(sa.getCity());
        addr.setDistrict(sa.getState());
        addr.setWard(sa.getPostalCode());
        return addr;
    }

    private Order finishOrder(String userId, OrderRequest request, List<OrderItem> orderItems, BigDecimal totalPrice,
            String shopId, com.shoppeclone.backend.user.model.Address resolvedAddress,
            List<CartItem> checkoutItems, Cart cart) {

        // 4. Calculate Discount (Product Voucher)
        BigDecimal productDiscount = BigDecimal.ZERO;
        String appliedProductVoucherCode = null;

        if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {
            appliedProductVoucherCode = processVoucher(userId, request.getVoucherCode(), totalPrice,
                    Voucher.VoucherType.PRODUCT, orderItems);
            productDiscount = calculateProductDiscount(appliedProductVoucherCode, totalPrice, orderItems);
        }

        // 5. Shipping Calculation (moved before shipping voucher processing)
        BigDecimal shippingFee = BigDecimal.ZERO;
        ShippingProvider provider = null;
        if (request.getShippingProviderId() != null) {
            provider = shippingProviderRepository.findById(request.getShippingProviderId())
                    .orElseThrow(() -> new RuntimeException("Shipping Provider not found"));
            shippingFee = shippingService.calculateShippingFee(provider.getId(), resolvedAddress, totalPrice);
        }

        // 6. Calculate Shipping Discount (Shipping Voucher)
        BigDecimal shippingDiscount = BigDecimal.ZERO;
        String appliedShippingVoucherCode = null;

        if (request.getShippingVoucherCode() != null && !request.getShippingVoucherCode().trim().isEmpty()) {
            if (shippingFee.compareTo(BigDecimal.ZERO) > 0) {
                appliedShippingVoucherCode = processVoucher(userId, request.getShippingVoucherCode(), totalPrice,
                        Voucher.VoucherType.SHIPPING, null);
                shippingDiscount = calculateDiscount(appliedShippingVoucherCode, shippingFee); // Apply against Shipping
                                                                                               // Fee
            }
        }

        // 6.b Calculate Shop Discount (Shop Voucher)
        BigDecimal shopDiscount = BigDecimal.ZERO;
        String appliedShopVoucherCode = null;
        if (request.getShopVoucherCode() != null && !request.getShopVoucherCode().trim().isEmpty()) {
            appliedShopVoucherCode = processShopVoucher(userId, request.getShopVoucherCode(), shopId, totalPrice);
            shopDiscount = calculateShopDiscount(appliedShopVoucherCode, totalPrice);
        }

        // 7. Create Order
        Order order = new Order();
        order.setUserId(userId);
        order.setShopId(shopId); // Set shopId
        order.setItems(orderItems);
        order.setVoucherCode(appliedProductVoucherCode);
        order.setShippingDiscount(shippingDiscount);
        order.setShippingVoucherCode(appliedShippingVoucherCode);
        order.setShopVoucherCode(appliedShopVoucherCode);
        order.setShopDiscount(shopDiscount);

        order.setTotalPrice(totalPrice.subtract(productDiscount).subtract(shopDiscount).add(shippingFee)
                .subtract(shippingDiscount));

        order.setOrderStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.UNPAID);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // 8. Shipping Info
        if (provider != null) {
            OrderShipping shipping = new OrderShipping();
            shipping.setProviderId(provider.getId());
            shipping.setAddress(resolvedAddress);
            shipping.setStatus("WAITING");
            shipping.setShippingFee(shippingFee);
            order.setShipping(shipping);
        }

        // 9. Save Order
        Order savedOrder = orderRepository.save(order);

        // 10. Remove ordered items from Cart (only when order came from cart)
        if (checkoutItems != null && cart != null) {
            for (CartItem item : checkoutItems) {
                cartService.removeCartItem(userId, item.getVariantId());
            }
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
    private String processVoucher(String userId, String code, BigDecimal orderValue, Voucher.VoucherType expectedType,
            List<OrderItem> orderItems) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Voucher not found: " + code));

        // Validate user hasn't already used this voucher
        List<Order> userOrders = orderRepository.findByUserId(userId);
        for (Order o : userOrders) {
            if (code.equals(o.getVoucherCode()) || code.equals(o.getShippingVoucherCode())) {
                throw new RuntimeException("Bạn đã sử dụng voucher này trong đơn hàng trước.");
            }
        }

        // Validate Type
        if (voucher.getType() != null && voucher.getType() != expectedType) {
            throw new RuntimeException(
                    "Invalid voucher type. Expected " + expectedType + " but got " + voucher.getType());
        }

        // Category restriction (PRODUCT only): must have eligible items
        if (expectedType == Voucher.VoucherType.PRODUCT && orderItems != null
                && voucher.getCategoryIds() != null && !voucher.getCategoryIds().isEmpty()) {
            BigDecimal eligibleAmount = getEligibleAmount(orderItems, voucher.getCategoryIds());
            if (eligibleAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException(
                        "Voucher chỉ áp dụng cho sản phẩm thuộc danh mục: " + voucher.getDescription());
            }
            // Min spend for category voucher: check against eligible amount
            if (voucher.getMinSpend() != null && eligibleAmount.compareTo(voucher.getMinSpend()) < 0) {
                throw new RuntimeException(
                        "Tổng tiền sản phẩm thuộc danh mục không đạt tối thiểu: " + voucher.getMinSpend());
            }
        } else {
            // Min Spend Check (for non-category or shipping)
            if (voucher.getMinSpend() != null && orderValue.compareTo(voucher.getMinSpend()) < 0) {
                throw new RuntimeException(
                        "Order total does not meet minimum spend for voucher: " + voucher.getMinSpend());
            }
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

        // Update Usage (Optimistic)
        voucher.setQuantity(voucher.getQuantity() - 1);
        voucher.setUsedCount(voucher.getUsedCount() == null ? 1 : voucher.getUsedCount() + 1);
        voucherRepository.save(voucher);

        return code;
    }

    /**
     * Sum of (price * quantity) for items whose product belongs to allowed
     * categories
     */
    private BigDecimal getEligibleAmount(List<OrderItem> orderItems, List<String> categoryIds) {
        if (orderItems == null || categoryIds == null || categoryIds.isEmpty())
            return BigDecimal.ZERO;
        BigDecimal sum = BigDecimal.ZERO;
        for (OrderItem item : orderItems) {
            ProductVariant variant = productVariantRepository.findById(item.getVariantId()).orElse(null);
            if (variant == null)
                continue;
            List<ProductCategory> pcs = productCategoryRepository.findByProductId(variant.getProductId());
            boolean eligible = pcs.stream().anyMatch(pc -> categoryIds.contains(pc.getCategoryId()));
            if (eligible) {
                sum = sum.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }
        return sum;
    }

    private BigDecimal calculateProductDiscount(String code, BigDecimal totalPrice, List<OrderItem> orderItems) {
        if (code == null)
            return BigDecimal.ZERO;
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Voucher not found during calc: " + code));
        BigDecimal baseAmount = totalPrice;
        if (voucher.getCategoryIds() != null && !voucher.getCategoryIds().isEmpty() && orderItems != null) {
            baseAmount = getEligibleAmount(orderItems, voucher.getCategoryIds());
        }
        return calculateDiscount(code, baseAmount);
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

    private String processShopVoucher(String userId, String code, String shopId, BigDecimal orderValue) {
        ShopVoucher voucher = shopVoucherRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Shop Voucher not found: " + code));

        if (!voucher.getShopId().equals(shopId)) {
            throw new RuntimeException("Voucher này không thuộc về Shop của đơn hàng này");
        }

        LocalDateTime now = LocalDateTime.now();
        if ((voucher.getStartDate() != null && now.isBefore(voucher.getStartDate()))
                || (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate()))) {
            throw new RuntimeException("Voucher đã hết hạn hoặc chưa đến ngày áp dụng");
        }

        if (voucher.getQuantity() <= 0) {
            throw new RuntimeException("Voucher đã hết lượt sử dụng");
        }

        if (voucher.getMinSpend() != null && orderValue.compareTo(voucher.getMinSpend()) < 0) {
            throw new RuntimeException(
                    "Giá trị đơn hàng tối thiểu để sử dụng Voucher này là " + voucher.getMinSpend().toString());
        }

        // Update Usage
        voucher.setQuantity(voucher.getQuantity() - 1);
        shopVoucherRepository.save(voucher);

        return code;
    }

    private BigDecimal calculateShopDiscount(String code, BigDecimal baseAmount) {
        if (code == null)
            return BigDecimal.ZERO;
        ShopVoucher voucher = shopVoucherRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Shop Voucher not found during calc: " + code));

        BigDecimal discount = voucher.getDiscount();
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
    @Transactional
    public Order updateOrderStatus(String orderId, OrderStatus status) {
        Order order = getOrder(orderId);
        OrderStatus previousStatus = order.getOrderStatus();

        // Update timestamps based on status (dùng thời gian thực khi seller đổi trạng
        // thái)
        if ((status == OrderStatus.SHIPPING || status == OrderStatus.SHIPPED) && order.getShippedAt() == null) {
            order.setShippedAt(LocalDateTime.now());
        }
        if (status == OrderStatus.COMPLETED && order.getCompletedAt() == null) {
            order.setCompletedAt(LocalDateTime.now());
        }

        // Sync product.sold when transitioning TO COMPLETED
        if (status == OrderStatus.COMPLETED && previousStatus != OrderStatus.COMPLETED) {
            for (OrderItem item : order.getItems()) {
                ProductVariant variant = productVariantRepository.findById(item.getVariantId()).orElse(null);
                if (variant != null) {
                    com.shoppeclone.backend.product.entity.Product product = productRepository
                            .findById(variant.getProductId()).orElse(null);
                    if (product != null) {
                        product.setSold((product.getSold() != null ? product.getSold() : 0) + item.getQuantity());
                        product.setUpdatedAt(LocalDateTime.now());
                        productRepository.save(product);
                    }
                }
            }
        }

        order.setOrderStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order cancelOrder(String orderId) {
        Order order = getOrder(orderId);
        if (order.getOrderStatus() == OrderStatus.SHIPPING || order.getOrderStatus() == OrderStatus.SHIPPED
                || order.getOrderStatus() == OrderStatus.COMPLETED || order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Cannot cancel order in state: " + order.getOrderStatus());
        }

        // Restore Stock
        for (OrderItem item : order.getItems()) {
            ProductVariant variant = productVariantRepository.findById(item.getVariantId()).orElse(null);
            if (variant != null) {
                variant.setStock(variant.getStock() + item.getQuantity());

                // Restore Flash Sale Stock if applicable
                restoreFlashSaleItemStock(variant.getProductId(), variant.getId(), item.getQuantity());

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
    public void deleteAllUserOrders(String userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        // Restore stock for orders that haven't been cancelled yet
        for (Order order : orders) {
            if (order.getOrderStatus() != OrderStatus.CANCELLED
                    && order.getOrderStatus() != OrderStatus.SHIPPING
                    && order.getOrderStatus() != OrderStatus.SHIPPED
                    && order.getOrderStatus() != OrderStatus.COMPLETED) {
                for (OrderItem item : order.getItems()) {
                    ProductVariant variant = productVariantRepository.findById(item.getVariantId()).orElse(null);
                    if (variant != null) {
                        variant.setStock(variant.getStock() + item.getQuantity());
                        restoreFlashSaleItemStock(variant.getProductId(), variant.getId(), item.getQuantity());
                        productVariantRepository.save(variant);
                    }
                }
            }
        }
        orderRepository.deleteByUserId(userId);
    }

    private boolean updateFlashSaleItemStock(String productId, String variantId, int quantity) {
        // Find active Flash Sale Item
        List<com.shoppeclone.backend.promotion.flashsale.entity.FlashSaleItem> items = flashSaleItemRepository
                .findByProductIdAndStatus(productId, "APPROVED");

        for (com.shoppeclone.backend.promotion.flashsale.entity.FlashSaleItem item : items) {
            // Check if parent Flash Sale is ONGOING
            com.shoppeclone.backend.promotion.flashsale.entity.FlashSale slot = flashSaleRepository
                    .findById(item.getFlashSaleId()).orElse(null);

            if (slot != null && "ONGOING".equals(slot.getStatus())) {
                if (item.getVariantId() == null || item.getVariantId().equals(variantId)) {
                    int currentRemaining = item.getRemainingStock() != null ? item.getRemainingStock() : 0;
                    if (currentRemaining >= quantity) {
                        item.setRemainingStock(currentRemaining - quantity);
                        flashSaleItemRepository.save(item);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void restoreFlashSaleItemStock(String productId, String variantId, int quantity) {
        // Find active Flash Sale Item
        List<com.shoppeclone.backend.promotion.flashsale.entity.FlashSaleItem> items = flashSaleItemRepository
                .findByProductIdAndStatus(productId, "APPROVED");

        for (com.shoppeclone.backend.promotion.flashsale.entity.FlashSaleItem item : items) {
            com.shoppeclone.backend.promotion.flashsale.entity.FlashSale slot = flashSaleRepository
                    .findById(item.getFlashSaleId()).orElse(null);

            if (slot != null && "ONGOING".equals(slot.getStatus())) {
                if (item.getVariantId() == null || item.getVariantId().equals(variantId)) {
                    // 1. Restore Campaign Remaining Stock
                    item.setRemainingStock(
                            (item.getRemainingStock() != null ? item.getRemainingStock() : 0) + quantity);
                    flashSaleItemRepository.save(item);

                    // 2. Decrement Persistent Sold Counters
                    ProductVariant variant = productVariantRepository.findById(variantId).orElse(null);
                    if (variant != null) {
                        int currentFsSold = variant.getFlashSaleSold() != null ? variant.getFlashSaleSold() : 0;
                        variant.setFlashSaleSold(Math.max(0, currentFsSold - quantity));
                        productVariantRepository.save(variant);

                        com.shoppeclone.backend.product.entity.Product product = productRepository.findById(productId)
                                .orElse(null);
                        if (product != null) {
                            int prodFsSold = product.getFlashSaleSold() != null ? product.getFlashSaleSold() : 0;
                            product.setFlashSaleSold(Math.max(0, prodFsSold - quantity));
                            productRepository.save(product);
                        }
                    }
                    break;
                }
            }
        }
    }
}
