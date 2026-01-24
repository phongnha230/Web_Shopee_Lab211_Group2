package com.shoppeclone.backend.auth.service;

import com.shoppeclone.backend.auth.dto.response.AuthResponse;

public interface OAuthService {

    String getGoogleAuthUrl();

    AuthResponse authenticateWithGoogle(String code);
}
