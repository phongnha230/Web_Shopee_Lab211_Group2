package com.shoppeclone.backend.auth.controller;

import com.shoppeclone.backend.auth.dto.response.AuthResponse;
import com.shoppeclone.backend.auth.service.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oauthService;

    // 👉 FE gọi để lấy link Google
    @GetMapping("/google/url")
    public ResponseEntity<Map<String, String>> getGoogleAuthUrl() {
        String url = oauthService.getGoogleAuthUrl();
        return ResponseEntity.ok(Map.of("authUrl", url));
    }

    // 👉 FE gửi code về để đổi JWT
    @PostMapping("/google/exchange")
    public ResponseEntity<AuthResponse> exchangeGoogleCode(
            @RequestBody Map<String, String> body) {

        String code = body.get("code");
        System.out.println("DEBUG: OAuthController received code: " + code);
        AuthResponse auth = oauthService.authenticateWithGoogle(code);
        return ResponseEntity.ok(auth);
    }
}
