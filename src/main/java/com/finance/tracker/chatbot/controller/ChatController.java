package com.finance.tracker.chatbot.controller;

import com.finance.tracker.chatbot.rag.context.FinancialContext;
import com.finance.tracker.chatbot.rag.document.DocumentFactory;
import com.finance.tracker.chatbot.rag.document.FinancialDocument;
import com.finance.tracker.chatbot.rag.document.MonthlySummaryDocumentBuilder;
import com.finance.tracker.chatbot.services.FinancialContextService;
import com.finance.tracker.dto.chatbot.ChatRequest;
import com.finance.tracker.dto.chatbot.ChatResponse;
import com.finance.tracker.service.LLMService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final FinancialContextService financialContextService;
    private final LLMService llmService;
    private final MonthlySummaryDocumentBuilder factory;


    @PostMapping(value = "/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatResponse> chat(
            @RequestBody ChatRequest request
    ) {
        try {
            FinancialContext context = financialContextService.getMonthlyContext("U1002", YearMonth.now());
            FinancialDocument document =
                    factory.build(context);
            System.out.println(document.getDocument().getText());
            String message = request.getMessage();

            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        ChatResponse.builder()
                                .success(false)
                                .message("Message cannot be empty")
                                .build()
                );
            }

            String response = llmService.askLLM(message);

            System.out.println("✓ Chatbot response: " + response);

            ChatResponse chatResponse = ChatResponse.builder()
                    .success(true)
                    .message("Success")
                    .data(ChatResponse.ChatData.builder()
                            .response(response)
                            .conversationId(request.getConversationId())
                            .build())
                    .build();

            System.out.println("✓ Chatbot response object: " + chatResponse);
            return ResponseEntity.ok(chatResponse);
        } catch (Exception e) {
            System.err.println("✗ Chatbot error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                    ChatResponse.builder()
                            .success(false)
                            .message("Error: " + e.getMessage())
                            .build()
            );
        }
    }
}
