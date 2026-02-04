package com.shoppeclone.backend.order.dto;

import com.shoppeclone.backend.user.model.Address;
import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private String shippingProviderId;
    private Address address;
    private String paymentMethod; // "COD", "BANKING"
    private String voucherCode; // Product Voucher
    private String shippingVoucherCode; // Free Shipping Voucher
    private List<String> variantIds; // If null/empty -> checkout all from cart
}
