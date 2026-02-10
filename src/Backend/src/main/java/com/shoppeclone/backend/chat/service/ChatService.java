package com.shoppeclone.backend.chat.service;

import com.shoppeclone.backend.chat.entity.Conversation;
import com.shoppeclone.backend.chat.entity.Message;
import com.shoppeclone.backend.chat.repository.ConversationRepository;
import com.shoppeclone.backend.chat.repository.MessageRepository;
import com.shoppeclone.backend.auth.model.User;
import com.shoppeclone.backend.auth.repository.UserRepository;
import com.shoppeclone.backend.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ShopRepository shopRepository;

    @Transactional
    public Conversation startConversation(String userId, String shopId) {
        if (!shopRepository.existsById(shopId)) {
            throw new RuntimeException("Shop not found");
        }

        return conversationRepository.findByUserIdAndShopId(userId, shopId)
                .orElseGet(() -> {
                    Conversation conversation = new Conversation();
                    conversation.setUserId(userId);
                    conversation.setShopId(shopId);
                    conversation.setLastMessageTime(LocalDateTime.now());
                    return conversationRepository.save(conversation);
                });
    }

    public List<Message> getMessages(String conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Transactional
    public Message sendMessage(String conversationId, String senderId, String content, boolean isShopMessage) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setContent(content);
        message.setShopMessage(isShopMessage);
        message.setCreatedAt(LocalDateTime.now());

        messageRepository.save(message);

        conversation.setLastMessageContent(content);
        conversation.setLastMessageTime(LocalDateTime.now());
        conversationRepository.save(conversation);

        return message;
    }

    public Optional<Conversation> getConversation(String conversationId) {
        return conversationRepository.findById(conversationId);
    }

    public List<Conversation> getConversationsByShop(String shopId) {
        List<Conversation> allConversations = conversationRepository.findByShopId(shopId);

        // Deduplicate by userId, keeping the one with the latest message
        return new ArrayList<>(allConversations.stream()
                .collect(Collectors.toMap(
                        Conversation::getUserId,
                        c -> c,
                        (c1, c2) -> {
                            if (c1.getLastMessageTime() == null)
                                return c2;
                            if (c2.getLastMessageTime() == null)
                                return c1;
                            return c1.getLastMessageTime().isAfter(c2.getLastMessageTime()) ? c1 : c2;
                        }))
                .values());
    }

    @Transactional
    public void deleteMessage(String messageId, String userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin nhắn"));

        // Only sender can delete for now
        if (!message.getSenderId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xóa tin nhắn này");
        }

        messageRepository.deleteById(messageId);
    }

    @Transactional
    public void deleteConversation(String conversationId, String userId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hội thoại"));

        // Verify participant
        boolean isParticipant = conv.getUserId().equals(userId);
        if (!isParticipant && conv.getShopId() != null) {
            com.shoppeclone.backend.shop.entity.Shop shop = shopRepository.findById(conv.getShopId()).orElse(null);
            if (shop != null && shop.getOwnerId().equals(userId)) {
                isParticipant = true;
            }
        }

        if (!isParticipant) {
            throw new RuntimeException("Bạn không có quyền xóa hội thoại này");
        }

        messageRepository.deleteByConversationId(conversationId);
        conversationRepository.deleteById(conversationId);
    }
}
