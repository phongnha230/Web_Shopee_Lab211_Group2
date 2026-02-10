package com.shoppeclone.backend.follow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowerResponse {
    private String userId;
    private String userName;
    private String userAvatar;
    private String userEmail;
    private LocalDateTime followedAt;
}
