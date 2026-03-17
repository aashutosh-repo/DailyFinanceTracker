package com.finance.tracker.dto.chatbot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {
    private boolean success;
    private String message;
    private ChatData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatData {
        private String response;
        private String conversationId;
    }
}
