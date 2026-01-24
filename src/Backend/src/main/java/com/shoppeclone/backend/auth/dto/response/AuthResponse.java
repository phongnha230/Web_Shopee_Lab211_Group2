package com.shoppeclone.backend.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private UserDto user;
    private String message; // ← THÊM FIELD NÀY

    // Constructor cũ để không ảnh hưởng code hiện tại
    public AuthResponse(String accessToken, String refreshToken, String tokenType, UserDto user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.user = user;
    }
}