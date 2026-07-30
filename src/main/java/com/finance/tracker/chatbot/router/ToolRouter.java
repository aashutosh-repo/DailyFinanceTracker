package com.finance.tracker.chatbot.router;

import com.finance.tracker.chatbot.tool.AiTool;
import com.finance.tracker.chatbot.tool.AiToolRegistry;
import com.finance.tracker.chatbot.tool.ToolResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ToolRouter {

    private final AiToolRegistry registry;

    public ToolResult route(String userId, String question) {

        for (AiTool tool : registry.getTools()) {

            if (tool.supports(question)) {
                return tool.execute(userId, question);
            }
        }

        return ToolResult.builder()
                .handled(false)
                .toolName(null)
                .data(null)
                .build();
    }
}