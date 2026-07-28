package com.finance.tracker.chatbot.memory;

import com.finance.tracker.chatbot.constants.MessageRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ConversationServiceImpl implements ConversationService{

    private final ConversationRepository repository;
    private final ConversationMessageRepository messageRepository;

    @Override
    public UUID createConversation(String userId) {
        Conversation conversation = Conversation.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .title("new Conservation")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        repository.save(conversation);
        return conversation.getId();

    }

    @Override
    public Conversation getOrCreateConversation(UUID conversationId, String userId) {
        if(conversationId==null){
            UUID id = createConversation(userId);
            return repository.findByIdAndUserId(id,userId).orElse(null);
        }

        return messageRepository.findByConversationId(conversationId).orElse(null);
    }

    @Override
    public void saveUserMessage(UUID conversationId, String message) {
        saveMessage(conversationId, MessageRole.USER, message);
    }

    @Override
    public void saveAssistantMessage(UUID conversationId, String message) {
        saveMessage(conversationId, MessageRole.ASSISTANT, message);
    }

    @Override
    public List<ConversationMessage> getConversationHistory(UUID conversationId) {
        return messageRepository.findTop20ByConversationIdOrderByCreatedAtDesc(conversationId);
    }

    @Override
    public void updateConversationTimestamp(UUID conversationId) {
        repository.findById(conversationId)
                .ifPresent(conversation -> {
                    conversation.setUpdatedAt(LocalDateTime.now());
                    repository.save(conversation);
                });

    }

    private void saveMessage(UUID conversationId, MessageRole role, String message) {
        ConversationMessage entity = ConversationMessage.builder()
                .conversationId(conversationId)
                .role(role)
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();
        messageRepository.save(entity);
        updateConversationTimestamp(conversationId);
    }
}
