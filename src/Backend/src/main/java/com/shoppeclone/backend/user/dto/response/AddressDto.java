package com.shoppeclone.backend.user.dto.response;

import lombok.Data;

@Data
public class AddressDto {
    private String id;
    private String fullName;
    private String phone;
    private String address;
    private String city;
    private String district;
    private String ward;
    private boolean isDefault; // ✅ KHÔNG có "setIsDefault"
}
