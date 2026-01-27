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

    // 🔥 FIX: Khớp với Google Cloud Console redirect URI
    private static final String GOOGLE_REDIRECT_URI = "http://localhost:3000/auth/callback/google";

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
        String url = UriComponentsBuilder
                .fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", GOOGLE_REDIRECT_URI)
                .queryParam("response_type", "code")
                .queryParam("scope", "openid email profile")
                .queryParam("prompt", "consent")
                .build()
                .toUriString();

        System.out.println("🔍 DEBUG: Using Redirect URI: '" + GOOGLE_REDIRECT_URI + "'");
        System.out.println("🔍 DEBUG: Full Auth URL: " + url);

        return url;
    }

    @Override
    public AuthResponse authenticateWithGoogle(String code) {
        try {
            System.out.println("🔍 DEBUG: Starting Google authentication with code: " + code);

            String accessToken = exchangeCodeForToken(code);
            System.out.println("✅ DEBUG: Got access token");

            GoogleUserInfo googleUser = getUserInfo(accessToken);
            System.out
                    .println("✅ DEBUG: Got Google user info: " + (googleUser != null ? googleUser.getEmail() : "null"));

            if (googleUser == null) {
                throw new RuntimeException("Failed to get Google user info");
            }

            User user = findOrCreateUser(googleUser);
            System.out.println("✅ DEBUG: User found/created: " + (user != null ? user.getEmail() : "null"));

            if (user == null) {
                throw new RuntimeException("Failed to create or find user for email: " + googleUser.getEmail());
            }

            String jwtAccessToken = jwtUtil.generateAccessToken(user.getEmail());
            String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

            saveUserSession(user, refreshToken);

            return new AuthResponse(jwtAccessToken, refreshToken, "Bearer", mapToUserDto(user));
        } catch (Exception e) {
            System.err.println("❌ ERROR in authenticateWithGoogle: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Google authentication failed: " + e.getMessage(), e);
        }
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

        GoogleUserInfo userInfo = response.getBody();

        // 🔥 NULL CHECK: Validate response body
        if (userInfo == null) {
            System.out.println("❌ ERROR: Google UserInfo response body is null");
            throw new RuntimeException("Google UserInfo response is null");
        }

        System.out.println("✅ Got user info from Google: " + userInfo.getEmail());
        return userInfo;
    }

    private User findOrCreateUser(GoogleUserInfo googleUser) {
        // 🔥 NULL CHECK: Validate input
        if (googleUser == null) {
            throw new RuntimeException("GoogleUserInfo is null");
        }
        if (googleUser.getEmail() == null || googleUser.getEmail().isEmpty()) {
            throw new RuntimeException("Google user email is null or empty");
        }

        System.out.println("DEBUG: findOrCreateUser called for email: " + googleUser.getEmail());

        try {
            OAuthAccount oauthAccount = oauthAccountRepository
                    .findByProviderAndProviderId("google", googleUser.getId())
                    .orElse(null);

            System.out.println("DEBUG: Found existing OAuthAccount? " + (oauthAccount != null));

            User user;

            if (oauthAccount != null) {
                user = oauthAccount.getUser();
                System.out.println("DEBUG: User retrieved from OAuthAccount.");
                if (user == null) {
                    System.out.println(
                            "⚠️ WARNING: OAuthAccount exists but user is null! Falling back to email search...");
                    user = userRepository.findByEmail(googleUser.getEmail()).orElse(null);

                    if (user == null) {
                        System.out.println("DEBUG: User not found by email, creating new...");
                        user = createNewUser(googleUser);
                    } else {
                        System.out.println("DEBUG: User found by email: " + user.getEmail()
                                + ". Linking to existing OAuthAccount.");
                    }

                    // Link the user back to the OAuthAccount
                    oauthAccount.setUser(user);
                    oauthAccountRepository.save(oauthAccount);
                }
            } else {
                user = userRepository.findByEmail(googleUser.getEmail()).orElse(null);
                System.out.println("DEBUG: Found existing User by email? " + (user != null));

                if (user == null) {
                    user = createNewUser(googleUser);
                }

                try {
                    OAuthAccount oa = new OAuthAccount();
                    oa.setUser(user);
                    oa.setProvider("google");
                    oa.setProviderId(googleUser.getId());
                    oa.setEmail(googleUser.getEmail());
                    oa.setCreatedAt(LocalDateTime.now());

                    System.out.println("DEBUG: Saving new OAuthAccount for: " + googleUser.getEmail());
                    oauthAccountRepository.save(oa);
                    System.out.println("DEBUG: OAuthAccount saved successfully!");
                } catch (Exception e) {
                    System.out.println("❌ ERROR: Failed to save OAuthAccount: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("✅ DEBUG: Returning user with ID: " + user.getId());
            return user;

        } catch (Exception e) {
            System.out.println("❌ CRITICAL ERROR in findOrCreateUser: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create or find user: " + e.getMessage(), e);
        }
    }

    private User createNewUser(GoogleUserInfo googleUser) {
        System.out.println("DEBUG: Creating new user...");
        User user = new User();
        user.setEmail(googleUser.getEmail());
        user.setFullName(googleUser.getName());
        user.setPassword(null); // OAuth users don't have password
        user.setEmailVerified(true);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        Role role = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    System.out.println("⚠️ Role ROLE_USER not found. Creating it now...");
                    Role newRole = new Role();
                    newRole.setName("ROLE_USER");
                    newRole.setDescription("Regular User");
                    return roleRepository.save(newRole);
                });
        user.setRoles(Set.of(role));

        try {
            System.out.println("DEBUG: Saving new User: " + user.getEmail());
            user = userRepository.save(user);
            System.out.println("DEBUG: User saved with ID: " + user.getId());

            if (user == null || user.getId() == null) {
                throw new RuntimeException("User save failed - returned null");
            }
            return user;
        } catch (Exception e) {
            System.out.println("❌ ERROR: Failed to save user: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save user to database", e);
        }
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
