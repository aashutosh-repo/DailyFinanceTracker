package com.finance.tracker.chatbot.prompt;

import com.finance.tracker.chatbot.context.PromptContext;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FinancialPromptBuilder implements PromptBuilder {
    @Override
    public String buildPrompt(PromptContext context) {

        StringBuilder prompt = new StringBuilder();

        if (context.toolResult() != null) {

            prompt.append("""
                    Tool Output
                    -----------
                    """);

            prompt.append(context.toolResult().data());

            return prompt.toString();
        }

        if (context.documents().isEmpty()) {

            return "No financial documents found.";
        }

        prompt.append("""
                Financial Context
                -----------------
                               \s
               \s""");

        for (Document document : context.documents()) {

            prompt.append(document.getText())
                    .append("\n\n");
        }

        return prompt.toString();
    }
}
