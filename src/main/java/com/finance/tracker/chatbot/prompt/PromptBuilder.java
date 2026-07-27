package com.finance.tracker.chatbot.prompt;

import org.springframework.ai.document.Document;
import java.util.List;

public interface PromptBuilder {

    String buildPrompt(
            String question,
            List<Document> documents);
}