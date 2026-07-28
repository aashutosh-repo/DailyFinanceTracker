package com.finance.tracker.chatbot.services.impl;

import com.finance.tracker.chatbot.prompt.PromptBuilder;
import com.finance.tracker.chatbot.prompt.PromptOrchestrator;
import com.finance.tracker.chatbot.retrieval.RetrievalService;
import com.finance.tracker.chatbot.services.ChatService;
import com.finance.tracker.dto.chatbot.ChatResponse;
import com.finance.tracker.service.LLMService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final RetrievalService retrievalService;
    private final PromptOrchestrator promptOrchestrator;
    private final LLMService llmService;

    @Override
    public ChatResponse chat(String userId, String question) {
        long start = System.currentTimeMillis();
        List<Document> documents = retrievalService.retrieveRelevantDocuments(question, userId);

        String prompt = promptOrchestrator.buildPrompt(question, documents);

        String answer =
                llmService.getLLMResponse(prompt);

        long end = System.currentTimeMillis();


        return ChatResponse.builder()
                .success(true)
                .message("Success")
                .data(
                        ChatResponse.ChatData.builder()
                                .response(answer)
                                .conversationId(UUID.randomUUID().toString())
                                .retrievedDocuments(documents.size())
                                .responseTimeMs(end - start)
                                .build()
                )
                .build();
    }
}