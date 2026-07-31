package com.finance.tracker.chatbot.services.impl;

import com.finance.tracker.chatbot.context.PromptContext;
import com.finance.tracker.chatbot.memory.Conversation;
import com.finance.tracker.chatbot.memory.ConversationMessage;
import com.finance.tracker.chatbot.memory.ConversationService;
import com.finance.tracker.chatbot.orchestrator.ChatContext;
import com.finance.tracker.chatbot.orchestrator.ChatOrchestrator;
import com.finance.tracker.chatbot.prompt.PromptOrchestrator;
import com.finance.tracker.chatbot.services.ChatService;
import com.finance.tracker.dto.chatbot.ChatResponse;
import com.finance.tracker.service.LLMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final PromptOrchestrator promptOrchestrator;
    private final LLMService llmService;
    private final ChatOrchestrator chatOrchestrator;
    private final ConversationService conversationService;

    @Override
    public ChatResponse chat(String userId, String question, String conversationId) {
        long start = System.currentTimeMillis();
        UUID convUUID = praseConversationId(conversationId);
        ChatContext context = chatOrchestrator.prepareContext(userId, question);
        Conversation conversation = conversationService.getOrCreateConversation(convUUID, userId);
        List<ConversationMessage> conversationHistory = conversationService.getConversationHistory(conversation.getId());

        PromptContext promptContext = PromptContext.builder()
                        .question(question)
                        .toolResult(context.toolResult())
                        .documents(context.documents())
                        .questionType(context.questionType())
                        .build();

        String prompt = promptOrchestrator.buildPrompt(question, promptContext,conversationHistory);


        String answer = llmService.getLLMResponse(prompt);

        conversationService.saveAssistantMessage(conversation.getId(), answer);
        long end = System.currentTimeMillis();
        log.info("chat completed for user {} in {}ms" , userId, end-start);

        return ChatResponse.builder()
                .success(true)
                .message("Success")
                .data(
                        ChatResponse.ChatData.builder()
                                .response(answer)
                                .conversationId(conversation.getId().toString())
                                .responseTimeMs(end - start)
                                .build()
                )
                .build();
    }

    private UUID praseConversationId(String conversationId) {
        if(conversationId == null || conversationId.isBlank()){
            return null;
        }
        try {
            return UUID.fromString(conversationId);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid ConversationId format received : {}", conversationId);
            return null;
        }
    }
}