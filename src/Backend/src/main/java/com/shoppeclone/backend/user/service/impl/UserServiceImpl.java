package com.shoppeclone.backend.user.service.impl;

import com.shoppeclone.backend.auth.dto.response.UserDto;
import com.shoppeclone.backend.auth.model.Role;
import com.shoppeclone.backend.auth.model.User;
import com.shoppeclone.backend.auth.repository.UserRepository;
import com.shoppeclone.backend.user.dto.request.*;
import com.shoppeclone.backend.user.dto.response.AddressDto;
import com.shoppeclone.backend.user.model.Address;
import com.shoppeclone.backend.user.repository.AddressRepository;
import com.shoppeclone.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    // ========= PROFILE =========

    @Override
    public UserDto getProfile(String email) {
        return mapToUserDto(getUserByEmail(email));
    }

    @Override
    @Transactional
    public void updateProfile(String email, UpdateProfileRequest request) {
        User user = getUserByEmail(email);
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = getUserByEmail(email);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Password confirmation does not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // ========= ADDRESS =========

    @Override
    public List<AddressDto> getAddressesByEmail(String email) {
        User user = getUserByEmail(email);
        return addressRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToAddressDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addAddressByEmail(String email, AddressRequest request) {
        User user = getUserByEmail(email);

        if (request.isDefault()) {
            resetDefaultAddresses(user.getId());
        }

        Address address = new Address();
        address.setUserId(user.getId());
        address.setFullName(request.getFullName());
        address.setPhone(request.getPhone());
        address.setAddress(request.getAddress());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setWard(request.getWard());
        address.setDefault(request.isDefault());
        address.setCreatedAt(LocalDateTime.now());
        address.setUpdatedAt(LocalDateTime.now());

        addressRepository.save(address);
    }

    @Override
    @Transactional
    public void updateAddressByEmail(String email, String addressId, AddressRequest request) {
        User user = getUserByEmail(email);

        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (request.isDefault()) {
            resetDefaultAddresses(user.getId());
        }

        address.setFullName(request.getFullName());
        address.setPhone(request.getPhone());
        address.setAddress(request.getAddress());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setWard(request.getWard());
        address.setDefault(request.isDefault());
        address.setUpdatedAt(LocalDateTime.now());

        addressRepository.save(address);
    }

    @Override
    @Transactional
    public void deleteAddressByEmail(String email, String addressId) {
        User user = getUserByEmail(email);

        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));

        addressRepository.delete(address);
    }

    // ========= HELPER =========

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void resetDefaultAddresses(String userId) {
        List<Address> addresses = addressRepository.findByUserId(userId);
        addresses.forEach(addr -> {
            if (addr.isDefault()) {
                addr.setDefault(false);
                addressRepository.save(addr);
            }
        });
    }

    private UserDto mapToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setEmailVerified(user.isEmailVerified());
        dto.setRoles(
                user.getRoles()
                        .stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()));
        return dto;
    }

    private AddressDto mapToAddressDto(Address address) {
        AddressDto dto = new AddressDto();
        dto.setId(address.getId());
        dto.setFullName(address.getFullName());
        dto.setPhone(address.getPhone());
        dto.setAddress(address.getAddress());
        dto.setCity(address.getCity());
        dto.setDistrict(address.getDistrict());
        dto.setWard(address.getWard());
        dto.setDefault(address.isDefault());
        return dto;
    }
}
