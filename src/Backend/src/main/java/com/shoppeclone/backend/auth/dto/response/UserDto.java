package com.shoppeclone.backend.auth.dto.response;

import lombok.Data;
import java.util.Set;

@Data
public class UserDto {
    private String id;
    private String email;
    private String fullName;
    private String phone;
    private String avatar;
    private boolean emailVerified;
    private Set<String> roles;
}