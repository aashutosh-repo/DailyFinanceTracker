package com.finance.tracker.chatbot.prompt;

import com.finance.tracker.chatbot.context.PromptContext;
import org.springframework.ai.document.Document;
import java.util.List;

public interface PromptBuilder {

    String buildPrompt(PromptContext context);
}