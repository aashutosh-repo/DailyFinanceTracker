package com.finance.tracker.chatbot.memory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    Optional<Conversation> findByIdAndUserId(UUID id, String userId);
}