package com.shoppeclone.backend.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "New password must be at least 8 characters")
    @jakarta.validation.constraints.Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$", message = "Password must contain at least one uppercase letter, one lowercase letter, and one number")
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    @Size(min = 8, message = "Confirm password must be at least 8 characters")
    private String confirmPassword;
}
