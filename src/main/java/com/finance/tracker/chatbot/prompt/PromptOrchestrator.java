package com.finance.tracker.chatbot.prompt;

import com.finance.tracker.chatbot.context.PromptContext;
import com.finance.tracker.chatbot.orchestrator.ChatContext;
import com.finance.tracker.chatbot.system.SystemPromptProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PromptOrchestrator {

    private final PromptBuilder promptBuilder;
    private final SystemPromptProvider systemPromptProvider;

    public String buildPrompt(String question, PromptContext chatContext) {
        String context = promptBuilder.buildPrompt(chatContext);
        return """
                %s

                =================================================
                FINANCIAL CONTEXT
                =================================================

                %s

                =================================================
                USER QUESTION
                =================================================

                %s

                Provide the best possible answer using ONLY the financial context above.
                If the answer cannot be determined from the context, clearly state that the information is unavailable.
                """.formatted(
                        systemPromptProvider.getSystemPrompt(),
                        context,
                        question);
            }
        }
