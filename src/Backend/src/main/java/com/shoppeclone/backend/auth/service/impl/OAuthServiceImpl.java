package com.shoppeclone.backend.auth.service.impl;

import com.shoppeclone.backend.auth.dto.response.AuthResponse;
import com.shoppeclone.backend.auth.dto.response.GoogleUserInfo;
import com.shoppeclone.backend.auth.dto.response.UserDto;
import com.shoppeclone.backend.auth.model.*;
import com.shoppeclone.backend.auth.repository.*;
import com.shoppeclone.backend.auth.security.JwtUtil;
import com.shoppeclone.backend.auth.service.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OAuthServiceImpl implements OAuthService {

    // 🔥 FIX CỨNG redirect URI – KHÔNG TRUYỀN TỪ FE
    private static final String GOOGLE_REDIRECT_URI = "http://localhost:3000/auth/callback/google.html";

    private final UserRepository userRepository;
    private final OAuthAccountRepository oauthAccountRepository;
    private final RoleRepository roleRepository;
    private final UserSessionRepository sessionRepository;
    private final JwtUtil jwtUtil;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.provider.google.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
    private String userInfoUri;

    @Override
    public String getGoogleAuthUrl() {
        return UriComponentsBuilder
                .fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", GOOGLE_REDIRECT_URI)
                .queryParam("response_type", "code")
                .queryParam("scope", "openid email profile")
                .queryParam("prompt", "consent")
                .build()
                .toUriString();
    }

    @Override
    public AuthResponse authenticateWithGoogle(String code) {

        String accessToken = exchangeCodeForToken(code);
        GoogleUserInfo googleUser = getUserInfo(accessToken);
        User user = findOrCreateUser(googleUser);

        String jwtAccessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        saveUserSession(user, refreshToken);

        return new AuthResponse(jwtAccessToken, refreshToken, "Bearer", mapToUserDto(user));
    }

    // ====================== PRIVATE ======================

    private String exchangeCodeForToken(String code) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", GOOGLE_REDIRECT_URI);
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUri, request, Map.class);

            Map body = response.getBody();
            if (body == null || !body.containsKey("access_token")) {
                throw new RuntimeException("Google did not return access_token");
            }
            return body.get("access_token").toString();

        } catch (HttpClientErrorException e) {
            System.out.println("❌ GOOGLE TOKEN ERROR:");
            System.out.println(e.getResponseBodyAsString());
            throw e;
        }
    }

    private GoogleUserInfo getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<GoogleUserInfo> response = restTemplate.exchange(userInfoUri, HttpMethod.GET, entity,
                GoogleUserInfo.class);

        return response.getBody();
    }

    private User findOrCreateUser(GoogleUserInfo googleUser) {

        OAuthAccount oauthAccount = oauthAccountRepository
                .findByProviderAndProviderId("google", googleUser.getId())
                .orElse(null);

        User user;

        if (oauthAccount != null) {
            user = oauthAccount.getUser();
        } else {
            user = userRepository.findByEmail(googleUser.getEmail()).orElse(null);

            if (user == null) {
                user = new User();
                user.setEmail(googleUser.getEmail());
                user.setFullName(googleUser.getName());
                user.setEmailVerified(true);
                user.setCreatedAt(LocalDateTime.now());

                Role role = roleRepository.findByName("ROLE_USER")
                        .orElseThrow();
                user.setRoles(Set.of(role));

                userRepository.save(user);
            }

            OAuthAccount oa = new OAuthAccount();
            oa.setUser(user);
            oa.setProvider("google");
            oa.setProviderId(googleUser.getId());
            oa.setEmail(googleUser.getEmail());
            oa.setCreatedAt(LocalDateTime.now());

            oauthAccountRepository.save(oa);
        }

        return user;
    }

    private void saveUserSession(User user, String refreshToken) {
        UserSession session = new UserSession();
        session.setUser(user);
        session.setRefreshToken(refreshToken);
        session.setExpiresAt(LocalDateTime.now().plusDays(7));
        sessionRepository.save(session);
    }

    private UserDto mapToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRoles(
                user.getRoles().stream().map(Role::getName).collect(HashSet::new, Set::add, Set::addAll));
        return dto;
    }
}
