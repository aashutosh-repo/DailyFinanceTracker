package com.finance.tracker.chatbot.orchestrator;

import com.finance.tracker.chatbot.router.QuestionType;
import com.finance.tracker.chatbot.tool.ToolResult;
import lombok.Builder;
import org.springframework.ai.document.Document;

import java.util.List;

@Builder
public record ChatContext(

        ToolResult toolResult,
        List<Document> documents,
        QuestionType questionType

) { }