package com.finance.tracker.chatbot.context;

import com.finance.tracker.chatbot.tool.ToolResult;
import lombok.Builder;
import org.springframework.ai.document.Document;

import java.util.List;

@Builder
public record PromptContext(

        String question,
        ToolResult toolResult,
        List<Document> documents

) {
}