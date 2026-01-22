package com.shoppeclone.backend.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GoogleUserInfo {
    @JsonProperty("sub")
    private String id;

    private String email;

    @JsonProperty("email_verified")
    private Boolean emailVerified;

    private String name;

    @JsonProperty("picture")
    private String picture;

    @JsonProperty("given_name")
    private String givenName;

    @JsonProperty("family_name")
    private String familyName;
}