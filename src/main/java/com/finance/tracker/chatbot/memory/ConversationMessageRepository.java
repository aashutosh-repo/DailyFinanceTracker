package com.finance.tracker.chatbot.memory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {

    List<ConversationMessage> findTop20ByConversationIdOrderByCreatedAtDesc(UUID conversationId);
    Optional<Conversation> findByConversationId(UUID conversationId);

}
