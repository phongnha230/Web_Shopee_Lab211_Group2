package com.shoppeclone.backend.user.controller;

import com.shoppeclone.backend.auth.dto.response.UserDto;
import com.shoppeclone.backend.user.dto.request.*;
import com.shoppeclone.backend.user.dto.response.AddressDto;
import com.shoppeclone.backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    private String getEmail(Authentication authentication) {
        return authentication.getName();
    }

    // ========= PROFILE =========

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getProfile(Authentication authentication) {
        return ResponseEntity.ok(
                userService.getProfile(getEmail(authentication)));
    }

    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {

        userService.updateProfile(getEmail(authentication), request);
        return ResponseEntity.ok("Profile updated successfully");
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        userService.changePassword(getEmail(authentication), request);
        return ResponseEntity.ok("Password changed successfully");
    }

    // ========= ADDRESS =========

    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDto>> getAddresses(Authentication authentication) {
        return ResponseEntity.ok(
                userService.getAddressesByEmail(getEmail(authentication)));
    }

    @PostMapping("/addresses")
    public ResponseEntity<String> addAddress(
            @Valid @RequestBody AddressRequest request,
            Authentication authentication) {

        userService.addAddressByEmail(getEmail(authentication), request);
        return ResponseEntity.ok("Address added successfully");
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<String> updateAddress(
            @PathVariable String addressId,
            @Valid @RequestBody AddressRequest request,
            Authentication authentication) {

        userService.updateAddressByEmail(getEmail(authentication), addressId, request);
        return ResponseEntity.ok("Address updated successfully");
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<String> deleteAddress(
            @PathVariable String addressId,
            Authentication authentication) {

        userService.deleteAddressByEmail(getEmail(authentication), addressId);
        return ResponseEntity.ok("Address deleted successfully");
    }

    // ADMIN
    @PostMapping("/promote")
    public ResponseEntity<String> promoteUser(@RequestParam String email, @RequestParam String roleName) {
        userService.promoteUser(email, roleName);
        return ResponseEntity.ok("User promoted to " + roleName + " successfully!");
    }
}
