package com.shoppeclone.backend.auth.dto.request;

import lombok.Data;

@Data
public class GoogleAuthRequest {
    private String code;
    private String redirectUri;
}