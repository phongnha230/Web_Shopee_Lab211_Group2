package com.shoppeclone.backend.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @Pattern(regexp = "^(0[0-9]{9})$", message = "Số điện thoại không hợp lệ")
    private String phone;
}
