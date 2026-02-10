package com.shoppeclone.backend.follow.service;

import com.shoppeclone.backend.follow.dto.FollowerResponse;
import com.shoppeclone.backend.follow.entity.Follow;
import com.shoppeclone.backend.follow.repository.FollowRepository;
import com.shoppeclone.backend.shop.repository.ShopRepository;
import com.shoppeclone.backend.auth.model.User;
import com.shoppeclone.backend.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    @Transactional
    public boolean toggleFollow(String userId, String shopId) {
        if (userId == null || shopId == null) {
            throw new IllegalArgumentException("UserId and ShopId must not be null");
        }
        // Verify shop exists
        if (!shopRepository.existsById(shopId)) {
            throw new RuntimeException("Shop not found");
        }

        Optional<Follow> existingFollow = followRepository.findByUserIdAndShopId(userId, shopId);
        if (existingFollow.isPresent()) {
            Follow follow = existingFollow.get();
            followRepository.delete(follow);
            return false; // Unfollowed
        } else {
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setShopId(shopId);
            followRepository.save(follow);
            return true; // Followed
        }
    }

    public boolean isFollowing(String userId, String shopId) {
        return followRepository.existsByUserIdAndShopId(userId, shopId);
    }

    public long getFollowerCount(String shopId) {
        return followRepository.countByShopId(shopId);
    }

    public java.util.List<FollowerResponse> listFollowers(String shopId) {
        if (shopId == null)
            return java.util.Collections.emptyList();
        java.util.List<Follow> follows = followRepository.findByShopId(shopId);
        return follows.stream().map(f -> {
            String fUserId = f.getUserId();
            User user = fUserId != null ? userRepository.findById(fUserId).orElse(null) : null;
            return FollowerResponse.builder()
                    .userId(f.getUserId())
                    .userName(user != null ? user.getFullName() : "Unknown User")
                    .userAvatar(user != null ? user.getAvatar() : null)
                    .userEmail(user != null ? user.getEmail() : null)
                    .followedAt(f.getCreatedAt())
                    .build();
        }).collect(java.util.stream.Collectors.toList());
    }
}
