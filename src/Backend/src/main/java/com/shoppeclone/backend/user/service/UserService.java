package com.shoppeclone.backend.user.service;

import com.shoppeclone.backend.auth.dto.response.UserDto;
import com.shoppeclone.backend.user.dto.request.*;
import com.shoppeclone.backend.user.dto.response.AddressDto;

import java.util.List;

public interface UserService {

    UserDto getProfile(String email);

    void updateProfile(String email, UpdateProfileRequest request);

    void changePassword(String email, ChangePasswordRequest request);

    List<AddressDto> getAddressesByEmail(String email);

    void addAddressByEmail(String email, AddressRequest request);

    void updateAddressByEmail(String email, String addressId, AddressRequest request);

    void deleteAddressByEmail(String email, String addressId);

    // ADMIN
    void promoteUser(String email, String roleName);
}
