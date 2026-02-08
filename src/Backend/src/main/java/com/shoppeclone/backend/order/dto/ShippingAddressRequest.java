package com.shoppeclone.backend.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ShippingAddressRequest {
    private String fullName;
    private String phone;
    private String street;
    private String city;
    private String state;
    @JsonProperty("postalCode")
    private String postalCode;
}
