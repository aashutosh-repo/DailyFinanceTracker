package com.finance.tracker.chatbot.controller;

import com.finance.tracker.chatbot.services.ChatService;
import com.finance.tracker.chatbot.services.FinancialContextService;
import com.finance.tracker.dto.chatbot.ChatRequest;
import com.finance.tracker.dto.chatbot.ChatResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;


    @PostMapping(value = "/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
            String message = request.getMessage();
            String userId = request.getUserId();

            try{
                ChatResponse response = chatService.chat(userId, message, request.getConversationId());
                return ResponseEntity.ok(response);
            }catch (Exception e){
                return ResponseEntity.status(500).body(ChatResponse.builder()
                        .success(false).message("UnExpected Error from LLM").build());
            }
    }
}
