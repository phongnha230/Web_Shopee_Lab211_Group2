package com.shoppeclone.backend.chat.repository;

import com.shoppeclone.backend.chat.entity.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {
    Optional<Conversation> findByUserIdAndShopId(String userId, String shopId);

    List<Conversation> findByUserId(String userId);

    List<Conversation> findByShopId(String shopId);
}
