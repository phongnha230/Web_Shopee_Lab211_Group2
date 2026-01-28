package com.shoppeclone.backend.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Pattern(regexp = "^(0[0-9]{9})$", message = "Phone number must be 10 digits and start with 0")
    private String phone;
}
