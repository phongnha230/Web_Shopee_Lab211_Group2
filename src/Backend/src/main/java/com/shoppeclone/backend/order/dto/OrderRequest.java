package com.shoppeclone.backend.order.dto;

import com.shoppeclone.backend.user.model.Address;
import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private String shippingProviderId;
    private Address address;
    /** Frontend sends shippingAddress - mapped here for compatibility */
    private ShippingAddressRequest shippingAddress;
    private String paymentMethod; // "COD", "BANKING"
    private String voucherCode; // Product Voucher
    private String shippingVoucherCode; // Free Shipping Voucher
    private List<String> variantIds; // If null/empty -> checkout all from cart
    /** For Buy Now mode - direct items instead of cart */
    private List<OrderItemRequest> items;

    // Explicit getters/setters (Lombok may not generate when annotation processor is off)
    public String getShippingProviderId() { return shippingProviderId; }
    public void setShippingProviderId(String v) { this.shippingProviderId = v; }
    public Address getAddress() { return address; }
    public void setAddress(Address v) { this.address = v; }
    public ShippingAddressRequest getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(ShippingAddressRequest v) { this.shippingAddress = v; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String v) { this.paymentMethod = v; }
    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String v) { this.voucherCode = v; }
    public String getShippingVoucherCode() { return shippingVoucherCode; }
    public void setShippingVoucherCode(String v) { this.shippingVoucherCode = v; }
    public List<String> getVariantIds() { return variantIds; }
    public void setVariantIds(List<String> v) { this.variantIds = v; }
    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> v) { this.items = v; }
}
