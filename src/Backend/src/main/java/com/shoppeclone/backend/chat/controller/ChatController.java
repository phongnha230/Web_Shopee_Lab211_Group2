package com.shoppeclone.backend.chat.controller;

import com.shoppeclone.backend.chat.entity.Conversation;
import com.shoppeclone.backend.chat.entity.Message;
import com.shoppeclone.backend.chat.service.ChatService;
import com.shoppeclone.backend.auth.model.User;
import com.shoppeclone.backend.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.shoppeclone.backend.shop.repository.ShopRepository;
import com.shoppeclone.backend.shop.entity.Shop;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    @PostMapping("/start/{shopId}")
    public ResponseEntity<?> startConversation(@PathVariable String shopId,
            @RequestParam(required = false) String userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null)
            return ResponseEntity.status(401).build();
        User currentUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        String targetUserId;
        if (userId != null && !userId.isEmpty()) {
            // Seller starting chat with specific follower
            Shop shop = shopRepository.findById(shopId).orElseThrow(() -> new RuntimeException("Shop not found"));
            if (!shop.getOwnerId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).body("Only shop owner can start chat with specific users");
            }
            targetUserId = userId;
        } else {
            // Buyer starting chat with shop
            targetUserId = currentUser.getId();
        }

        Conversation conversation = chatService.startConversation(targetUserId, shopId);
        return ResponseEntity.ok(Map.of("conversationId", conversation.getId()));
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<?> getMessages(@PathVariable String conversationId) {
        // In a real app, check permissions (if user is participant)
        List<Message> messages = chatService.getMessages(conversationId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<?> getConversation(@PathVariable String conversationId) {
        return chatService.getConversation(conversationId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/shop/{shopId}/conversations")
    public ResponseEntity<?> getConversationsByShop(@PathVariable String shopId) {
        // In real app, verify user owns this shop
        List<Conversation> conversations = chatService.getConversationsByShop(shopId);
        return ResponseEntity.ok(conversations);
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<?> sendMessage(@PathVariable String conversationId, @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null)
            return ResponseEntity.status(401).build();
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        String content = payload.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Content cannot be empty");
        }

        Conversation conv = chatService.getConversation(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        boolean isShopMessage = false;

        // Check if user is the owner of the shop in the conversation
        if (conv.getShopId() != null) {
            Shop shop = shopRepository.findById(conv.getShopId()).orElse(null);
            if (shop != null && shop.getOwnerId().equals(user.getId())) {
                isShopMessage = true;
            }
        }

        Message message = chatService.sendMessage(conversationId, user.getId(), content, isShopMessage);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<?> deleteMessage(@PathVariable String messageId,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null)
            return ResponseEntity.status(401).build();
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        try {
            chatService.deleteMessage(messageId, user.getId());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<?> deleteConversation(@PathVariable String conversationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null)
            return ResponseEntity.status(401).build();
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        try {
            chatService.deleteConversation(conversationId, user.getId());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
