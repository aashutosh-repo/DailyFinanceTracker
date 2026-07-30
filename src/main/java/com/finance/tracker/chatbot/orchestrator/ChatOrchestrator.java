package com.finance.tracker.chatbot.orchestrator;

import com.finance.tracker.chatbot.retrieval.RetrievalService;
import com.finance.tracker.chatbot.router.ToolRouter;
import com.finance.tracker.chatbot.tool.ToolResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatOrchestrator {

    private final ToolRouter toolRouter;
    private final RetrievalService retrievalService;

    public ChatContext prepareContext(
            String userId,
            String question) {

        ToolResult toolResult =
                toolRouter.route(userId, question);

        if (toolResult.handled()) {

            return ChatContext.builder()
                    .toolResult(toolResult)
                    .documents(List.of())
                    .build();
        }

        List<Document> documents =
                retrievalService.retrieveRelevantDocuments(
                        question,
                        userId);

        return ChatContext.builder()
                .toolResult(null)
                .documents(documents)
                .build();
    }
}