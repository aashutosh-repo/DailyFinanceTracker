package com.finance.tracker.chatbot.memory;

import java.util.List;
import java.util.UUID;

public interface ConversationService {

    UUID createConversation(String userId);
    Conversation getOrCreateConversation(UUID conversationId, String userId);
    void saveUserMessage(UUID conversationId, String message);
    void saveAssistantMessage(UUID conversationId, String message);
    List<ConversationMessage> getConversationHistory(UUID conversationId);
    void updateConversationTimestamp(UUID conversationId);

}
