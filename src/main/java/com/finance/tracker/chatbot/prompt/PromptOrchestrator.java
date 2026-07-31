package com.finance.tracker.chatbot.prompt;

import com.finance.tracker.chatbot.context.PromptContext;
import com.finance.tracker.chatbot.memory.ConversationMessage;
import com.finance.tracker.chatbot.system.SystemPromptProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PromptOrchestrator {

    private final PromptBuilder promptBuilder;
    private final SystemPromptProvider systemPromptProvider;

    public String buildPrompt(String question, PromptContext chatContext, List<ConversationMessage> history) {
        String context = promptBuilder.buildPrompt(chatContext);
        String historyBlock = buildHistoryBlock(history);
    return """
            %s
            =================================================
            CONVERSATION HISTORY
            =================================================
           
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
            Reference conversation history where relevant to give a continuous experience.
            If the answer cannot be determined from the context, clearly state that the information is unavailable.
           \s""".formatted(
                    systemPromptProvider.getSystemPrompt(),
                    historyBlock,
                    context,
                    question);
    }
    private String buildHistoryBlock(List<ConversationMessage> history ) {
        if(history == null || history.isEmpty()){
            return "No Prior Conversation History in this session ";
        }

        List<ConversationMessage> ordered = new ArrayList<>();
        Collections.reverse(ordered);
        StringBuilder sb = new StringBuilder();
        for (ConversationMessage msg : ordered){
            sb.append(msg.getRole())
                    .append(": ")
                    .append(msg.getMessage())
                    .append("\n");
        }
        return sb.toString().trim();
    }


}
