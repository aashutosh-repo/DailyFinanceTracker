package com.finance.tracker.chatbot.prompt;

import com.finance.tracker.chatbot.context.PromptContext;
import com.finance.tracker.chatbot.memory.ConversationMessage;
import com.finance.tracker.chatbot.router.QuestionType;
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
        if(chatContext.questionType() == QuestionType.GENERAL_KNOWLEDGE) {
            return  buildGeneralKnowledge(systemPromptProvider.getSystemPrompt(), historyBlock, question);
        }
        return buildPersonalDataPrompt(systemPromptProvider.getSystemPrompt(), historyBlock, context, question);
    }
    private String buildPersonalDataPrompt(String systemPrompt, String historyBlock, String context, String question) {
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
           \s""".formatted(systemPrompt, historyBlock, context, question);
    }

    private String buildGeneralKnowledge(String systemPrompt, String historyBlock, String question) {
        return """
                %s
                 =============================================
                 Conversation History
                ==============================================
                
                %s
                
                ==============================================
                USER QUESTION
                ==============================================
                
                %s
                
                This is a general knowledge finance question.
                Answer clearly in plain language using your training knowledge.
                Use a simple real-world example where it helps understanding.
                keep the answer concise(under 500 words) unless asked a detailed explanation
                is explicitly requested.
                End with one short, practical tip related to the topic.
                """
                .formatted(systemPrompt,historyBlock,question);
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
