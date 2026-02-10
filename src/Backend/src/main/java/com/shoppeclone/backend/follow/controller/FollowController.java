package com.shoppeclone.backend.follow.controller;

import com.shoppeclone.backend.follow.service.FollowService;
import com.shoppeclone.backend.auth.model.User;
import com.shoppeclone.backend.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowController {
    private final FollowService followService;
    private final UserRepository userRepository;

    @PostMapping("/{shopId}")
    public ResponseEntity<?> toggleFollow(@PathVariable String shopId,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        boolean isFollowing = followService.toggleFollow(user.getId(), shopId);
        return ResponseEntity.ok(Map.of(
                "isFollowing", isFollowing,
                "message", isFollowing ? "Followed successfully" : "Unfollowed successfully"));
    }

    @GetMapping("/check/{shopId}")
    public ResponseEntity<?> checkFollowStatus(@PathVariable String shopId,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.ok(Map.of("isFollowing", false));
        }

        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        boolean isFollowing = followService.isFollowing(user.getId(), shopId);
        return ResponseEntity.ok(Map.of("isFollowing", isFollowing));
    }

    @GetMapping("/count/{shopId}")
    public ResponseEntity<?> getFollowerCount(@PathVariable String shopId) {
        long count = followService.getFollowerCount(shopId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/shop/{shopId}/list")
    public ResponseEntity<?> listFollowers(@PathVariable String shopId) {
        return ResponseEntity.ok(followService.listFollowers(shopId));
    }
}
