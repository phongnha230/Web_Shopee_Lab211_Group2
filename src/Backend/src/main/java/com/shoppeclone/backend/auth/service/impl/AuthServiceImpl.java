package com.shoppeclone.backend.auth.service.impl;

import com.shoppeclone.backend.auth.dto.request.LoginRequest;
import com.shoppeclone.backend.auth.dto.request.RegisterRequest;
import com.shoppeclone.backend.auth.dto.response.AuthResponse;
import com.shoppeclone.backend.auth.dto.response.UserDto;
import com.shoppeclone.backend.auth.model.Role;
import com.shoppeclone.backend.auth.model.User;
import com.shoppeclone.backend.auth.model.UserSession;
import com.shoppeclone.backend.auth.repository.RoleRepository;
import com.shoppeclone.backend.auth.repository.UserRepository;
import com.shoppeclone.backend.auth.repository.UserSessionRepository;
import com.shoppeclone.backend.auth.security.JwtUtil;
import com.shoppeclone.backend.auth.service.AuthService;
import com.shoppeclone.backend.auth.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        System.out.println("========== REGISTER START ==========");
        System.out.println("Email: " + request.getEmail());

        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            System.out.println("ERROR: Email already exists!");
            throw new RuntimeException("Email đã tồn tại");
        }

        // Tạo user mới - PENDING STATE
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActive(true);
        user.setEmailVerified(false); // ❌ CHƯA VERIFY OTP

        // Set default role
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role not found"));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        // Lưu user PENDING vào database
        userRepository.save(user);
        System.out.println("✅ User saved PENDING: " + user.getEmail());

        // ✅ GỬI OTP TỰ ĐỘNG
        String otpMessage = "";
        try {
            otpService.sendOtpEmail(user.getEmail());
            otpMessage = "Đăng ký thành công! Mã OTP đã được gửi đến email của bạn.";
            System.out.println("✅ OTP sent to: " + user.getEmail());
        } catch (Exception e) {
            otpMessage = "Đăng ký thành công nhưng không thể gửi OTP. Vui lòng yêu cầu gửi lại!";
            System.err.println("⚠️ OTP sending error: " + e.getMessage());
        }

        // ❌ KHÔNG TRẢ TOKEN - Frontend tự chuyển OTP page
        AuthResponse response = new AuthResponse(null, null, null, mapToUserDto(user));
        response.setMessage(otpMessage);

        System.out.println("========== REGISTER SUCCESS - PENDING OTP ==========");
        return response;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        System.out.println("========== LOGIN START ==========");
        System.out.println("Email: " + request.getEmail());

        // Tìm user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        // Kiểm tra password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            System.out.println("ERROR: Wrong password!");
            throw new RuntimeException("Sai mật khẩu");
        }

        // 🚨 BLOCK NẾU CHƯA VERIFY OTP (dùng emailVerified)
        if (!user.isEmailVerified()) {
            System.out.println("ERROR: Email not verified!");
            throw new RuntimeException("Vui lòng xác thực OTP trước khi đăng nhập");
        }

        // Kiểm tra account active
        if (!user.isActive()) {
            System.out.println("ERROR: Account is inactive!");
            throw new RuntimeException("Tài khoản đã bị khóa");
        }

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        // Save session
        saveUserSession(user, refreshToken);

        System.out.println("========== LOGIN SUCCESS ==========");
        return new AuthResponse(accessToken, refreshToken, "Bearer", mapToUserDto(user));
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        System.out.println("========== REFRESH TOKEN START ==========");

        // Validate token
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("Refresh token không hợp lệ");
        }

        // Tìm session
        UserSession session = sessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Session không tồn tại"));

        User user = session.getUser();

        // CHECK USER ĐÃ VERIFY
        if (!user.isEmailVerified()) {
            throw new RuntimeException("Tài khoản chưa được xác thực OTP");
        }

        // Generate new tokens
        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        // Delete old session
        sessionRepository.delete(session);

        // Save new session
        saveUserSession(user, newRefreshToken);

        System.out.println("========== REFRESH TOKEN SUCCESS ==========");
        return new AuthResponse(newAccessToken, newRefreshToken, "Bearer", mapToUserDto(user));
    }

    @Override
    public void logout(String refreshToken) {
        System.out.println("========== LOGOUT ==========");
        sessionRepository.deleteByRefreshToken(refreshToken);
        System.out.println("✅ Session deleted");
    }

    private void saveUserSession(User user, String refreshToken) {
        UserSession session = new UserSession();
        session.setUser(user);
        session.setRefreshToken(refreshToken);
        session.setExpiresAt(LocalDateTime.now().plusDays(7));
        session.setCreatedAt(LocalDateTime.now());
        sessionRepository.save(session);
        System.out.println("✅ Session saved");
    }

    private UserDto mapToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setEmailVerified(user.isEmailVerified());
        dto.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));
        return dto;
    }
}
