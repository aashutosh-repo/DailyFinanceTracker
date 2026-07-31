package com.finance.tracker.chatbot.controller;

import com.finance.tracker.chatbot.retrieval.RetrievalService;
import com.finance.tracker.chatbot.services.ChatService;
import com.finance.tracker.dto.chatbot.ChatRequest;
import com.finance.tracker.dto.chatbot.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bot")
@RequiredArgsConstructor
public class RetrievalTestController {

    private final RetrievalService retrievalService;
    private final ChatService chatService;

    @GetMapping("/search")
    public List<Document> search(
            @RequestParam String question,
            @RequestParam String userId) {

        return retrievalService.retrieveRelevantDocuments(
                question,
                userId);
    }
    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {

        return chatService.chat(request.getUserId(), request.getMessage(),request.getConversationId());

    }
}