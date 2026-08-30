package com.finance.tracker.controller;


import com.finance.tracker.dto.chatbot.ChatRequest;
import com.finance.tracker.dto.chatbot.ChatResponse;
import com.finance.tracker.service.impl.DocumentsReader;
import com.finance.tracker.service.impl.LLMServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/llm")
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600, allowedHeaders = "*")
@RequiredArgsConstructor
public class LLMController {

    private final LLMServiceImpl llmService;
    private final ObjectProvider<DocumentsReader> readerProvider;


    @PostMapping(value = "/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        try {
            String message = request.getMessage();

            String response = llmService.askLLM(message);

            ChatResponse chatResponse = ChatResponse.builder()
                    .success(true)
                    .message("Success")
                    .data(ChatResponse.ChatData.builder()
                            .response(response)
                            .conversationId(request.getConversationId())
                            .build())
                    .build();

            return ResponseEntity.ok(chatResponse);
        } catch (Exception e) {
            log.error("✗ Chatbot error: {}" , e.getMessage(),e);
            return ResponseEntity.status(500).body(
                ChatResponse.builder()
                    .success(false)
                    .message("Unexpected Error: " + e.getMessage())
                    .build()
            );
        }
    }

    @GetMapping("/embeddings")
    public ResponseEntity<String> embeddings() throws Exception {
        DocumentsReader reader = readerProvider.getIfAvailable();
        if (reader == null) {
            return ResponseEntity.status(503).body("Vector Store is Not Enable");
        }
        reader.readDocument("docs/SYSTEM_ARCHITECTURE.md");
        return ResponseEntity.ok("Embeddings created and stored successfully.");
    }

    @GetMapping("/prompt")
    public ResponseEntity<String> getAnswer() {
        DocumentsReader reader = readerProvider.getIfAvailable();
        if (reader == null) {
            return ResponseEntity.status(503).body("Vector Store is Not Enable");
        }
        String ans = reader.getAnswer();
        return ResponseEntity.ok(ans);
    }
}